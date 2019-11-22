package com.phantom.acceptor.zookeeper;

import com.phantom.acceptor.config.AcceptorConfig;
import com.phantom.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * 管理Zookeeper
 *
 * @author Jianfeng Wang
 * @since 2019/11/20 14:08
 */
@Slf4j
public class ZookeeperManager {

    private AcceptorConfig config;
    private CuratorFramework framework;
    private String selfPath;

    public ZookeeperManager(AcceptorConfig config) {
        this.config = config;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        framework = CuratorFrameworkFactory.newClient(config.getZookeeperServer(), retryPolicy);
        framework.start();
    }

    /**
     * 创建当前服务节点
     */
    public void createNode() {
        try {
            selfPath = Constants.ZK_ACCEPTOR_PATH + "/" + InetAddress.getLocalHost().getHostAddress() +
                    ":" + config.getPort();
            log.info("往Zookeeper注册临时节点：{}", selfPath);
            if (framework.checkExists().forPath(selfPath) != null) {
                framework.delete().forPath(selfPath);
            }
            framework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(selfPath,
                    "0".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 为改Acceptor系统增加一个客户端
     */
    public void incrementClient() {
        InterProcessMutex interProcessMutex = new InterProcessMutex(framework, Constants.ZK_ACCEPTOR_CLIENT_LOCK +
                "/" + selfPath.substring(selfPath.lastIndexOf("/") + 1));
        try {
            interProcessMutex.acquire();
            byte[] bytes = framework.getData().forPath(selfPath);
            String data = new String(bytes, StandardCharsets.UTF_8);
            int count = Integer.parseInt(data);
            count++;
            log.info("客户端连接 + 1，更新到zk ：{} -> {}", selfPath, count);
            framework.setData().forPath(selfPath, String.valueOf(count).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                interProcessMutex.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 为改Acceptor系统清除一个客户端
     */
    public void decrementClient() {
        InterProcessMutex interProcessMutex = new InterProcessMutex(framework, Constants.ZK_ACCEPTOR_CLIENT_LOCK +
                "/" + selfPath.substring(selfPath.lastIndexOf("/") + 1));
        try {
            interProcessMutex.acquire();
            byte[] bytes = framework.getData().forPath(selfPath);
            String data = new String(bytes, StandardCharsets.UTF_8);
            int count = Integer.parseInt(data);
            count--;
            log.info("客户端连接 - 1，更新到zk ：{} -> {}", selfPath, count);
            framework.setData().forPath(selfPath, String.valueOf(count).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                interProcessMutex.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
