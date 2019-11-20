package com.zhss.im.acceptor.dispatcher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 分发系统实例
 *
 * @author Jianfeng Wang
 */
@Data
@AllArgsConstructor
@Builder
public class DispatcherInstanceAddress {
    /**
     * IP地址
     */
    private String ip;
    /**
     * 端口
     */
    private int port;
}
