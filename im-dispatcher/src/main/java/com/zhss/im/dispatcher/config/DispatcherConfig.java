package com.zhss.im.dispatcher.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;
import java.util.Properties;

/**
 * 分发服务器配置
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 16:37
 */
@Data
@Builder
public class DispatcherConfig {

    /**
     * 消息最大长度
     */
    private int maxMessageBytes = 4096;

    /**
     * redis服务器地址
     */
    private String redisServer = null;

    /**
     * zookeeper地址
     */
    private String zookeeperServer = null;

    /**
     * 监听端口
     */
    private int port;

    /**
     * 分发线程数量
     */
    private int threadNum;

    /**
     * 阻塞队列大小
     */
    private int queueSize;

    /**
     * kafka服务器地址
     */
    private String kafkaBrokers;

    /**
     * 解析配置文件
     *
     * @param configFileName 配置文件名
     * @return 配置
     */
    public static DispatcherConfig parse(String configFileName) throws Exception {
        InputStream resourceAsStream = DispatcherConfig.class.getClassLoader().getResourceAsStream(configFileName);
        Properties properties = new Properties();
        properties.load(resourceAsStream);
        int maxMessageBytes = Integer.valueOf(properties.getProperty("max.message.bytes"));
        String redisServer = properties.getProperty("redis.server");
        String zookeeperServer = properties.getProperty("zookeeper.server");
        int port = Integer.valueOf(properties.getProperty("dispatcher.server.port"));
        int threadNum = Integer.valueOf(properties.getProperty("io.thread.num"));
        int queueSize = Integer.valueOf(properties.getProperty("io.queue.size"));
        String kafkaBrokers = properties.getProperty("kafka.broker.list");
        return DispatcherConfig.builder()
                .maxMessageBytes(maxMessageBytes)
                .redisServer(redisServer)
                .zookeeperServer(zookeeperServer)
                .port(port)
                .threadNum(threadNum)
                .queueSize(queueSize)
                .kafkaBrokers(kafkaBrokers)
                .build();

    }


}
