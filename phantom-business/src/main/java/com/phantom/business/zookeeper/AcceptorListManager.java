package com.phantom.business.zookeeper;

import com.phantom.common.Constants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 接入服务器管理
 *
 * @author Jianfeng Wang
 * @since 2019/11/20 10:44
 */
@Slf4j
@Component
public class AcceptorListManager implements InitializingBean {

    @Resource
    private CuratorFramework framework;

    /**
     * 保存了连接地址
     */
    private Set<String> ipList = new HashSet<>();

    /**
     * 保存了每个连接地址有多少个客户端，按升序排序
     */
    private Set<Node> nodeList = new TreeSet<>((o1, o2) -> {
        if (o1.ipAddress.equals(o2.ipAddress)) {
            return 0;
        }
        return o1.count - o2.count;
    });

    public String getFirst() {
        for (Node node : nodeList) {
            return node.ipAddress;
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Stat stat = framework.checkExists().forPath(Constants.ZK_ACCEPTOR_PATH);
        if (stat == null) {
            framework.create().creatingParentsIfNeeded().forPath(Constants.ZK_ACCEPTOR_PATH);
        }
        processAcceptorListChanged();
    }


    @AllArgsConstructor
    public static class Node {
        String ipAddress;
        Integer count;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Node node = (Node) o;
            return Objects.equals(ipAddress, node.ipAddress) &&
                    Objects.equals(count, node.count);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ipAddress, count);
        }

        @Override
        public String toString() {
            return "Node{" +
                    "ipAddress='" + ipAddress + '\'' +
                    ", count=" + count +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "AcceptorListManager{" +
                "ipList=" + ipList +
                ", nodeList=" + nodeList +
                '}';
    }

    /**
     * 对于ZK的数据而言，大概是这样的：
     * <p>
     * /zhss-im/acceptor/
     * ------http://localhost:8019   10
     * ------http://localhost:8090   2
     * <p>
     * 节点路径表示ip地址，节点内容表示该节点目标有多少个客户端连接
     */
    private void processAcceptorListChanged() {
        try {
            List<String> children = framework.getChildren().forPath(Constants.ZK_ACCEPTOR_PATH);
            if (children.isEmpty()) {
                log.info("接入系统列表为空，清除内存缓存");
                ipList.clear();
                nodeList.clear();
                addParentWatcher();
                return;
            }
            // handle new node
            for (String child : children) {
                if (!ipList.contains(child)) {
                    log.info("接入系统上线，添加地址：{}", child);
                    ipList.add(child);
                    getAcceptorClientCount(framework, Constants.ZK_ACCEPTOR_PATH + "/" + child, child);
                    addChildWatcher(child);
                }
            }

            // handle remove node
            Iterator<String> iterator = ipList.iterator();
            while (iterator.hasNext()) {
                String existsIP = iterator.next();
                if (!children.contains(existsIP)) {
                    log.info("接入系统下线，移除地址：{}", existsIP);
                    iterator.remove();
                    nodeList.removeIf(node -> node.ipAddress.equals(existsIP));
                }
            }
            addParentWatcher();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addChildWatcher(String child) throws Exception {
        framework.getData()
                .usingWatcher((Watcher) event -> {
                    if (event.getType() == Watcher.Event.EventType.NodeDataChanged) {
                        try {
                            getAcceptorClientCount(framework, event.getPath(), child);
                            addChildWatcher(child);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                })
                .forPath(Constants.ZK_ACCEPTOR_PATH + "/" + child);
    }

    private void addParentWatcher() {
        try {
            framework.getChildren().usingWatcher((Watcher) event -> {
                Watcher.Event.EventType type = event.getType();
                log.info("收到事件: {}", event);
                if (type == Watcher.Event.EventType.NodeChildrenChanged) {
                    processAcceptorListChanged();
                }
            }).forPath(Constants.ZK_ACCEPTOR_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAcceptorClientCount(CuratorFramework framework, String path, String ipAddress) throws Exception {
        byte[] bytes = framework.getData().forPath(path);
        String data = new String(bytes, StandardCharsets.UTF_8);
        int count = Integer.parseInt(data);
        log.info("保存接入系统的客户端数量：{} -> {}", path, count);
        Node node = new Node(ipAddress, count);
        nodeList.add(node);
    }

}
