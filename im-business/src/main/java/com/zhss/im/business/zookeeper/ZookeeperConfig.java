package com.zhss.im.business.zookeeper;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * zk 配置
 *
 * @author Jianfeng Wang
 * @since 2019/11/20 10:24
 */
@Slf4j
@Configuration
public class ZookeeperConfig {

    @Value("${zookeeper.server}")
    private String serverUrl;

    @Bean
    public CuratorFramework curatorFramework() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework framework = CuratorFrameworkFactory.newClient(serverUrl, retryPolicy);
        framework.start();
        return framework;
    }
}
