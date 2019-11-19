package com.zhss.im.business.kafka;

import com.alibaba.fastjson.JSONObject;
import com.zhss.im.business.kafka.consumer.Acknowledgement;
import com.zhss.im.business.kafka.consumer.SingleMessageListener;
import com.zhss.im.business.kafka.producer.Producer;
import com.zhss.im.business.mapper.C2gMessageMapper;
import com.zhss.im.business.mapper.ConversationMembersMapper;
import com.zhss.im.common.Constants;
import com.zhss.im.common.model.KafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 单聊消息消费
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 16:00
 */
@Slf4j
@Component
public class C2gMessageListener implements SingleMessageListener {

    @Resource
    private C2gMessageMapper c2gMessageMapper;

    @Resource
    private Producer producer;

    @Resource
    private SnowflakeIdWorker snowflakeIdWorker;

    @Resource
    private ConversationMembersMapper conversationMembersMapper;

    @Override
    public String getTopic() {
        return Constants.TOPIC_SEND_C2G_MESSAGE;
    }

    @Override
    public void onMessage(String message, Acknowledgement acknowledgement) {
        log.info("收到消息：{}", message);
        KafkaMessage c2gMessage = JSONObject.parseObject(message, KafkaMessage.class);
        c2gMessage.setMessageId(snowflakeIdWorker.nextId());
        c2gMessageMapper.saveMessage(c2gMessage);

        // send response
        String value = JSONObject.toJSONString(c2gMessage);
        // 发送给发送者的响应，按照senderId做partition hash
        producer.send(Constants.TOPIC_SEND_C2G_MESSAGE_RESPONSE, c2gMessage.getSenderId(), value);

        List<Long> membersIds =
                conversationMembersMapper.getMembersByConversationId(Long.valueOf(c2gMessage.getGroupId()));
        // push message, 按照receiverId做partition hash
        KafkaMessage kafkaMessage = KafkaMessage.builder()
                .senderId(c2gMessage.getSenderId())
                .groupId(c2gMessage.getGroupId())
                .content(c2gMessage.getContent())
                .timestamp(c2gMessage.getTimestamp())
                .messageId(c2gMessage.getMessageId())
                .groupUId(membersIds.stream()
                        .map(String::valueOf)
                        .filter(e -> !e.equals(c2gMessage.getSenderId()))
                        .collect(Collectors.toList()))
                .build();
        producer.send(Constants.TOPIC_PUSH_MESSAGE, kafkaMessage.getGroupId(), JSONObject.toJSONString(kafkaMessage));

        // send kafka ack
        acknowledgement.ack();
    }
}
