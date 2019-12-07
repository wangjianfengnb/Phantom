package com.phantom.acceptor.config;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;
import java.util.Properties;

/**
 * 接入系统配置
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 15:44
 */
@Data
@Builder
public class AcceptorConfig {

    /**
     * 消息最大长度
     */
    private int maxMessageBytes;

    /**
     * redis服务器地址
     */
    private String redisServer;

    /**
     * zookeeper地址
     */
    private String zookeeperServer;

    /**
     * 监听端口
     */
    private int port;

    /**
     * 主机名
     */
    private String hostname;

    /**
     * 线程数量
     */
    private int coreSize;

    /**
     * SSL秘钥文件
     */
    private String keyStore;

    private String sslPassword;

    public static AcceptorConfig parse(String configFileName) throws Exception {
        InputStream resourceAsStream = AcceptorConfig.class.getClassLoader().getResourceAsStream(configFileName);
        Properties properties = new Properties();
        properties.load(resourceAsStream);
        int maxMessageBytes = Integer.valueOf(properties.getProperty("max.message.bytes"));
        String redisServer = properties.getProperty("redis.server");
        String zookeeperServer = properties.getProperty("zookeeper.server");
        int port = Integer.valueOf(properties.getProperty("acceptor.port"));
        String hostname = properties.getProperty("acceptor.hostname");
        int thread = Integer.valueOf(properties.getProperty("io.thread"));
        String keyStore = properties.getProperty("ssl.keystore");
        String password = properties.getProperty("ssl.password");
        return AcceptorConfig.builder()
                .maxMessageBytes(maxMessageBytes)
                .redisServer(redisServer)
                .zookeeperServer(zookeeperServer)
                .port(port)
                .coreSize(thread)
                .keyStore(keyStore)
                .sslPassword(password)
                .hostname(hostname)
                .build();

    }
}
