package com.phantom.business.kafka;

import com.alibaba.fastjson.JSONObject;
import com.phantom.business.kafka.consumer.SingleMessageListener;
import com.phantom.business.kafka.producer.Producer;
import com.phantom.business.mapper.C2gMessageMapper;
import com.phantom.business.mapper.GroupMembersMapper;
import com.phantom.business.kafka.consumer.Acknowledgement;
import com.phantom.common.Constants;
import com.phantom.common.model.KafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

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
    private GroupMembersMapper groupMembersMapper;

    @Override
    public String getTopic() {
        return Constants.TOPIC_SEND_C2G_MESSAGE;
    }

    @Override
    public void onMessage(String message, Acknowledgement acknowledgement) {
        log.info("收到消息：{}", message);
        KafkaMessage c2gMessage = JSONObject.parseObject(message, KafkaMessage.class);
        List<String> membersIds =
                groupMembersMapper.getMembersByConversationId(Long.valueOf(c2gMessage.getGroupId()));
        if (CollectionUtils.isEmpty(membersIds)) {
            log.info("无法找到群聊信息：不处理该消息");
            return;
        }
        c2gMessage.setMessageId(snowflakeIdWorker.nextId());
        c2gMessageMapper.saveMessage(c2gMessage);

        // send response
        String value = JSONObject.toJSONString(c2gMessage);
        // 发送给发送者的响应，按照senderId做partition hash
        producer.send(Constants.TOPIC_SEND_C2G_MESSAGE_RESPONSE, c2gMessage.getSenderId(), value);

        // push message, 按照receiverId做partition hash
        KafkaMessage kafkaMessage = KafkaMessage.builder()
                .senderId(c2gMessage.getSenderId())
                .groupId(c2gMessage.getGroupId())
                .content(c2gMessage.getContent())
                .timestamp(c2gMessage.getTimestamp())
                .messageId(c2gMessage.getMessageId())
                .groupUId(membersIds.stream()
                        .filter(e -> !e.equals(c2gMessage.getSenderId()))
                        .collect(Collectors.toList()))
                .crc(c2gMessage.getCrc())
                .platform(c2gMessage.getPlatform())
                .build();
        producer.send(Constants.TOPIC_PUSH_MESSAGE, kafkaMessage.getGroupId(), JSONObject.toJSONString(kafkaMessage));

        // send kafka ack
        acknowledgement.ack();
    }
}
