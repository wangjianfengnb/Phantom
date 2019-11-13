package com.zhss.im.business.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * kafka客户端
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 17:41
 */
@Slf4j
@Component
public class Producer implements InitializingBean {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServer;

    private KafkaProducer<String, String> producer;

    public Producer() {
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
                log.info("发送到Kafka成功");
            } else {
                log.error("发送消息到Kafka失败：", exception);
            }
        });
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServer);
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
}
