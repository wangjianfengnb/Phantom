package com.zhss.im.business.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * Kafka消费消息处理
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 13:47
 */

@Slf4j
@Component
public class Consumer implements InitializingBean {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServer;

    /**
     * 消息处理器列表
     */
    @Autowired(required = false)
    private List<MessageListener> messageListeners = null;


    @Override
    public void afterPropertiesSet() throws Exception {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", bootstrapServer);
        properties.put("group.id", "business-group");
        properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put("enable.auto.commit", "false");
        properties.put("auto.commit.interval.ms", "1000");
        properties.put("auto.offset.reset", "earliest");
        initConsumer(properties);
    }

    private void initConsumer(Properties properties) {
        if (messageListeners == null || messageListeners.isEmpty()) {
            return;
        }
        checkMessageListener();
        for (MessageListener messageListener : messageListeners) {
            Processor processor = new Processor(messageListener, properties);
            processor.start();
        }
    }

    private void checkMessageListener() {
        Set<String> topics = new HashSet<>();
        for (MessageListener listener : messageListeners) {
            String t = listener.getTopic();
            if (StringUtils.isEmpty(t)) {
                throw new IllegalArgumentException("topic名称为空");
            }
            if (topics.contains(t)) {
                throw new IllegalArgumentException("topic名称重复：" + t);
            } else {
                topics.add(t);
            }
        }
    }

    private boolean isRun() {
        return true;
    }

    private class Processor extends Thread {

        private MessageListener messageListener;

        private KafkaConsumer<String, String> consumer;

        public Processor(MessageListener messageListener, Properties properties) {
            this.messageListener = messageListener;
            this.consumer = new KafkaConsumer<>(properties);
            setDaemon(true);
            setName("Kafka-Processor-Topic-" + messageListener.getTopic());
            this.consumer.subscribe(Collections.singleton(messageListener.getTopic()));
        }

        @Override
        public void run() {
            while (isRun()) {
                try {
                    ConsumerRecords<String, String> records = this.consumer.poll(1000);
                    if (records == null || records.isEmpty()) {
                        continue;
                    }
                    Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>(500);
                    if (messageListener instanceof BatchMessageListener) {
                        Iterator<ConsumerRecord<String, String>> iterator = records.iterator();
                        List<String> messages = new ArrayList<>();
                        // default max poll 500 message
                        while (iterator.hasNext()) {
                            ConsumerRecord<String, String> record = iterator.next();
                            String value = record.value();
                            messages.add(value);
                            offsets.put(new TopicPartition(record.topic(), record.partition()),
                                    new OffsetAndMetadata(record.offset() + 1));
                        }
                        BatchMessageListener listener = (BatchMessageListener) messageListener;
                        listener.onMessage(messages, () -> this.consumer.commitSync(offsets));
                        offsets.clear();
                    } else if (messageListener instanceof SingleMessageListener) {
                        SingleMessageListener listener = (SingleMessageListener) messageListener;
                        for (ConsumerRecord<String, String> record : records) {
                            String value = record.value();
                            offsets.put(new TopicPartition(record.topic(), record.partition()),
                                    new OffsetAndMetadata(record.offset() + 1));
                            listener.onMessage(value, () -> this.consumer.commitSync(offsets));
                            offsets.clear();
                        }
                    } else {
                        throw new IllegalArgumentException("Unknown MessageListener messageType: " + messageListener);
                    }
                } catch (Exception e) {
                    log.error("消费消息产生错误：" + e);
                }
            }
        }
    }
}
