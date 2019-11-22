package com.phantom.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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
public class KafkaMessage {

    /**
     * 发送者ID
     */
    private String senderId;
    /**
     * 接受者ID
     */
    private String receiverId;
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
    private Long messageId;

    /**
     * 群聊ID
     */
    private String groupId;

    /**
     * 消息严格的顺序
     */
    private long sequence;

    /**
     * 群聊消息接收者ID
     */
    private List<String> groupUId;

}
