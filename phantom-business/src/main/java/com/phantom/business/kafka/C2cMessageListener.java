package com.phantom.business.kafka;

import com.alibaba.fastjson.JSONObject;
import com.phantom.business.kafka.consumer.SingleMessageListener;
import com.phantom.business.kafka.producer.Producer;
import com.phantom.business.mapper.C2cMessageMapper;
import com.phantom.business.kafka.consumer.Acknowledgement;
import com.phantom.common.Constants;
import com.phantom.common.model.KafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 单聊消息消费
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 16:00
 */
@Slf4j
@Component
public class C2cMessageListener implements SingleMessageListener {

    @Resource
    private C2cMessageMapper c2CMessageMapper;

    @Resource
    private Producer producer;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Override
    public String getTopic() {
        return Constants.TOPIC_SEND_C2C_MESSAGE;
    }

    @Override
    public void onMessage(String message, Acknowledgement acknowledgement) {
        log.info("收到消息：{}", message);
        KafkaMessage c2cMessage = JSONObject.parseObject(message, KafkaMessage.class);
        c2cMessage.setMessageId(snowflakeIdWorker.nextId());
        c2CMessageMapper.saveMessage(c2cMessage);


        // send response
        String value = JSONObject.toJSONString(c2cMessage);
        // 发送给发送者的响应，按照senderId做partition hash
        producer.send(Constants.TOPIC_SEND_C2C_MESSAGE_RESPONSE, c2cMessage.getSenderId(), value);

        // push message, 按照receiverId做partition hash
        KafkaMessage kafkaMessage = KafkaMessage.builder()
                .senderId(c2cMessage.getSenderId())
                .receiverId(c2cMessage.getReceiverId())
                .content(c2cMessage.getContent())
                .timestamp(c2cMessage.getTimestamp())
                .messageId(c2cMessage.getMessageId())
                .crc(c2cMessage.getCrc())
                .platform(c2cMessage.getPlatform())
                .build();
        producer.send(Constants.TOPIC_PUSH_MESSAGE, kafkaMessage.getReceiverId(), JSONObject.toJSONString(kafkaMessage));

        // send kafka ack
        acknowledgement.ack();
    }
}
