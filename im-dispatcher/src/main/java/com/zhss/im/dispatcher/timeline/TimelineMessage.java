package com.zhss.im.dispatcher.timeline;

import com.zhss.im.common.model.PushMessage;
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
    private long groupId;

    /**
     * 消息严格的顺序
     */
    private long sequence;

    public static TimelineMessage parseC2CMessage(PushMessage pushMessage) {
        return TimelineMessage.builder()
                .senderId(pushMessage.getSenderId())
                .receiverId(pushMessage.getReceiverId())
                .content(pushMessage.getContent())
                .timestamp(pushMessage.getTimestamp())
                .build();
    }


    public static List<TimelineMessage> parseC2GMessage(PushMessage pushMessage) {
        List<TimelineMessage> messages = new ArrayList<>(pushMessage.getGroupUId().size());
        for (String receiverId : pushMessage.getGroupUId()) {
            TimelineMessage message = TimelineMessage.builder()
                    .senderId(pushMessage.getSenderId())
                    .receiverId(receiverId)
                    .content(pushMessage.getContent())
                    .timestamp(pushMessage.getTimestamp())
                    .groupId(pushMessage.getGroupId())
                    .build();
            messages.add(message);
        }
        return messages;

    }
}
