package com.zhss.im.dispatcher.mq;

import com.zhss.im.dispatcher.config.DispatcherConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import java.util.*;

/**
 * Kafka消费端
 * <p>
 * 消费端由于处理业务不同，为了不同的topic消费能力相互不影响，为每一个topic都建立一个consumer
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 18:46
 */
@Slf4j
public class Consumer {

    /**
     * kafka consumer
     */
    private KafkaConsumer<String, String> consumer;

    /**
     * 消息监听器
     */
    @Setter
    private MessageListener messageListener;

    public Consumer(DispatcherConfig dispatcherConfig, String... topics) {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", dispatcherConfig.getKafkaBrokers());
        properties.put("group.id", "business-group");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("enable.auto.commit", "false");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("auto.offset.reset", "earliest");
        this.consumer = new KafkaConsumer<>(properties);
        this.consumer.subscribe(Arrays.asList(topics));
        log.info("初始化kafkaConsumer........");
        new Processor().start();
    }


    class Processor extends Thread {
        @Override
        public void run() {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(1000);
                if (records == null || records.isEmpty()) {
                    continue;
                }
                Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>(500);
                for (ConsumerRecord<String, String> record : records) {
                    String value = record.value();
                    if (messageListener != null) {
                        messageListener.onMessage(value);
                    }
                    offsets.put(new TopicPartition(record.topic(), record.partition()),
                            new OffsetAndMetadata(record.offset() + 1));
                }
                consumer.commitSync(offsets);
                offsets.clear();
            }
        }
    }
}
