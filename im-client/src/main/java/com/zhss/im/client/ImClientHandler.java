package com.zhss.im.client;

import com.zhss.im.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * IM 客户端处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 13:06
 */
public class ImClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Connected to Acceptor Server : " + ctx.channel());
        ConnectManager connectManager = ConnectManager.getInstance();
        connectManager.onChannelActive((SocketChannel) ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到消息");
        ByteBuf byteBuf = (ByteBuf) msg;
        Message message = Message.parse(byteBuf);
        ConnectManager connectManager = ConnectManager.getInstance();
        connectManager.onMessageReceive(message);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("连接断开：" + ctx);
        ConnectManager connectManager = ConnectManager.getInstance();
        connectManager.onChannelInActive((SocketChannel) ctx.channel());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        System.out.println("发生异常：" + cause.getMessage());
        ConnectManager connectManager = ConnectManager.getInstance();
        connectManager.onChannelInActive((SocketChannel) ctx.channel());
    }
}
