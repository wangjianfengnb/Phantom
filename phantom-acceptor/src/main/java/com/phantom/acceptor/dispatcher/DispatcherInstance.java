package com.phantom.acceptor.dispatcher;

import io.netty.channel.socket.SocketChannel;
import lombok.Builder;


/**
 * 分发系统实例
 *
 * @author Jianfeng Wang
 * @since 2019/11/7 15:56
 */
@Builder
public class DispatcherInstance {

    /**
     * 渠道
     */
    private SocketChannel channel;

    /**
     * 发送消息
     *
     * @param msg 消息
     */
    public void sendMessage(Object msg) {
        channel.writeAndFlush(msg);
    }
}
