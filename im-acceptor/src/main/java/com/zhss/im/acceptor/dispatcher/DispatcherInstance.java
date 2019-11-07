package com.zhss.im.acceptor.dispatcher;

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

    private SocketChannel channel;

    public void sendMsg(Object msg) {
        channel.writeAndFlush(msg);
    }
}
