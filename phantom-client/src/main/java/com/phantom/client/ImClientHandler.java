package com.phantom.client;

import com.phantom.common.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * IM 客户端处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 13:06
 */
@Slf4j
public class ImClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("与接入系统建立连接：{}", ctx.channel());
        ConnectionManager.getInstance().setChannel((SocketChannel) ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf byteBuf = (ByteBuf) msg;
            Message message = Message.parse(byteBuf);
            ConnectionManager.getInstance().onReceiveMessage(message);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接断开：{}", ctx);
        ConnectionManager.getInstance().setChannel(null);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("客户端发生了异常：{}", cause.getMessage());
    }
}
