package com.zhss.im.dispatcher.message;

import lombok.Builder;
import lombok.Data;

/**
 * C2C的消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 18:01
 */
@Builder
@Data
public class C2CMessage {
    /**
     * 发送者ID
     */
    private String senderId;
    /**
     * 接受者ID
     */
    private String receiverId;
    /**
     * 内容
     */
    private String content;
    /**
     * 消息ID
     */
    private Long messageId;

    /**
     * 时间戳
     */
    private Long timestamp;

}

