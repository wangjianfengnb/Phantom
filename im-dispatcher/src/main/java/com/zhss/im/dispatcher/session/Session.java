package com.zhss.im.dispatcher.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代表一个客户端回话
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 14:36
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    /**
     * 用户ID
     */
    private String uid;

    /**
     * 用户标识
     */
    private String token;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 接入系统id
     */
    private String acceptorChannelId;
}
