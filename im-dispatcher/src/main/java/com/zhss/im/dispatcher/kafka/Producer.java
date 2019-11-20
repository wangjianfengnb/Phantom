package com.zhss.im.dispatcher.kafka;

import com.zhss.im.dispatcher.config.DispatcherConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * kafka客户端
 * <p>
 * 生产者做成单例
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 17:41
 */
@Slf4j
public class Producer {


    private static volatile Producer instance = null;

    /**
     * 获取实例
     *
     * @param config 配置
     * @return 生产者
     */
    public static Producer getInstance(DispatcherConfig config) {
        if (instance == null) {
            synchronized (Producer.class) {
                if (instance == null) {
                    instance = new Producer(config);
                }
            }
        }
        return instance;
    }

    /**
     * kafka producer
     */
    private KafkaProducer<String, String> producer;


    private Producer(DispatcherConfig dispatcherConfig) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", dispatcherConfig.getKafkaBrokers());
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("acks", "-1");
        properties.put("max.in.flight.requests.per.connection", "1");
        properties.put("retries", Integer.MAX_VALUE);
        properties.put("batch.size", 16 * 1024);
        properties.put("linger.ms", 10);
        properties.put("buffer.memory", 32 * 1024 * 1024);
        this.producer = new KafkaProducer<>(properties);
        log.info("初始化kafkaProducer........");
    }

    /**
     * 发送
     *
     * @param topic topic
     * @param key   key
     * @param value value
     */
    public void send(String topic, String key, String value) {
        this.producer.send(new ProducerRecord<>(topic, key, value), (metadata, exception) -> {
            if (exception == null) {
                log.info("发送消息到Kafka成功");
            } else {
                log.error("发送消息到Kafka失败：", exception);
            }
        });
    }


}
