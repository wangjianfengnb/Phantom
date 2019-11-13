package com.zhss.im.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 推送消息,发送到Kafka的消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/13 13:42
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PushMessage {

    /**
     * 单聊消息
     */
    public static final int MESSAGE_TYPE_C2C = 1;

    /**
     * 群聊消息
     */
    public static final int MESSAGE_TYPE_C2G = 2;

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
    private int type;
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


}
