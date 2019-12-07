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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 管理Zookeeper
 * <p>
 * 主要做两件事情：
 * 1. 在接入系统启动的时候，将自己注册到{@link Constants#ZK_ACCEPTOR_PATH} 中
 * 2. 当客户端连接数量改变的时候，将数量更新到zk节点中
 * <p>
 * 因为客户端连接和断开连接是一件频繁的事情，所以不能每次客户端数量发生变化都增加或者减少数量
 * <p>
 * 所以在后台维护一个线程，定时将数据更新到zookeeper中
 *
 * @author Jianfeng Wang
 * @since 2019/11/20 14:08
 */
@Slf4j
public class ZookeeperManager {

    /**
     * 配置
     */
    private AcceptorConfig config;
    /**
     * Zookeeper客户端
     */
    private CuratorFramework framework;

    /**
     * 注册地址
     */
    private String selfPath;

    /**
     * 客户端数量
     */
    private AtomicInteger clientCount = new AtomicInteger(0);

    /**
     * 上一个周期客户端数量
     */
    private AtomicInteger lastPeriodClientCount = new AtomicInteger(0);

    public ZookeeperManager(AcceptorConfig config) {
        this.config = config;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        framework = CuratorFrameworkFactory.newClient(config.getZookeeperServer(), retryPolicy);
        framework.start();
        new SyncThread().start();
    }

    /**
     * 创建当前服务节点
     */
    public void createNode() {
        try {
            selfPath = Constants.ZK_ACCEPTOR_PATH + "/" + InetAddress.getByName(config.getHostname()).getHostAddress() +
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
     * 为该Acceptor系统增加一个客户端
     */
    public void incrementClient() {
        clientCount.incrementAndGet();
    }

    /**
     * 为该Acceptor系统清除一个客户端
     */
    public void decrementClient() {
        clientCount.decrementAndGet();
    }

    private class SyncThread extends Thread {
        @Override
        public void run() {
            for (; ; ) {
                try {
                    maybeUpdateClientCount();
                    Thread.sleep(60 * 1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 更新客户端数量
     */
    private void maybeUpdateClientCount() {
        if (clientCount.get() == lastPeriodClientCount.get()) {
            log.info("客户端数量没有变化，不更新到zk, count = {}", clientCount.get());
            return;
        }
        InterProcessMutex interProcessMutex = new InterProcessMutex(framework, Constants.ZK_ACCEPTOR_CLIENT_LOCK +
                "/" + selfPath.substring(selfPath.lastIndexOf("/") + 1));
        try {
            interProcessMutex.acquire();
            log.info("更新客户端数量到zk ：{} -> {}", selfPath, clientCount.get());
            lastPeriodClientCount.set(clientCount.get());
            framework.setData().forPath(selfPath, String.valueOf(clientCount.get()).getBytes());
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
