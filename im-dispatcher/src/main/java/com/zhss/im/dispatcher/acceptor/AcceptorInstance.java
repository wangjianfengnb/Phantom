package com.zhss.im.dispatcher.acceptor;

import io.netty.channel.socket.SocketChannel;
import lombok.Builder;
import lombok.Data;

/**
 * 接入系统实例信息
 *
 * @author Jianfeng Wang
 * @since 2019/11/6 14:20
 */
@Builder
@Data
public class AcceptorInstance {

    /**
     * 渠道
     */
    private SocketChannel channel;
}
