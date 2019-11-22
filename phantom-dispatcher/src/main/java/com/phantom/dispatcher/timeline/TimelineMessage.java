package com.phantom.dispatcher.timeline;

import com.phantom.common.model.KafkaMessage;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * timeline 消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/18 10:47
 */
@Builder
@Data
public class TimelineMessage implements Serializable {

    /**
     * 发送者ID
     */
    private String senderId;
    /**
     * 接受者ID
     */
    private String receiverId;
    /**
     * 消息类型
     */
    private int messageType;
    /**
     * 消息内容
     */
    private String content;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 消息ID
     */
    private long messageId;

    /**
     * 群聊ID
     */
    private String groupId;

    /**
     * 消息严格的顺序
     */
    private long sequence;

    public static TimelineMessage parseC2CMessage(KafkaMessage kafkaMessage) {
        return TimelineMessage.builder()
                .senderId(kafkaMessage.getSenderId())
                .receiverId(kafkaMessage.getReceiverId())
                .content(kafkaMessage.getContent())
                .timestamp(kafkaMessage.getTimestamp())
                .messageId(kafkaMessage.getMessageId())
                .build();
    }


    public static List<TimelineMessage> parseC2GMessage(KafkaMessage kafkaMessage) {
        List<TimelineMessage> messages = new ArrayList<>(kafkaMessage.getGroupUId().size());
        for (String receiverId : kafkaMessage.getGroupUId()) {
            TimelineMessage message = TimelineMessage.builder()
                    .senderId(kafkaMessage.getSenderId())
                    .receiverId(receiverId)
                    .content(kafkaMessage.getContent())
                    .timestamp(kafkaMessage.getTimestamp())
                    .groupId(kafkaMessage.getGroupId())
                    .messageId(kafkaMessage.getMessageId())
                    .build();
            messages.add(message);
        }
        return messages;

    }
}
