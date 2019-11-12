package com.zhss.im.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * C2C的消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 18:01
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class C2cMessage {
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

