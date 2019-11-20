package com.zhss.im.dispatcher.zookeeper;

import com.zhss.im.common.Constants;
import com.zhss.im.dispatcher.config.DispatcherConfig;
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

    private DispatcherConfig config;
    private CuratorFramework framework;
    private String selfPath;

    public ZookeeperManager(DispatcherConfig config) {
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
            selfPath = Constants.ZK_DISPATCH_PATH + "/" + InetAddress.getLocalHost().getHostAddress() +
                    ":" + config.getPort();
            log.info("往Zookeeper注册临时节点：{}", selfPath);
            if (framework.checkExists().forPath(selfPath) != null) {
                framework.delete().forPath(selfPath);
            }
            framework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(selfPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
