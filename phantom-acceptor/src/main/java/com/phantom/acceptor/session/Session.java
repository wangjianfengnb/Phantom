package com.phantom.acceptor.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

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
public class Session implements Serializable {

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
    private String acceptorInstanceId;
}
