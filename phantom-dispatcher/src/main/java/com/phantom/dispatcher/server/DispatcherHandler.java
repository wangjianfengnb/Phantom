package com.phantom.dispatcher.server;

import com.phantom.common.Message;
import com.phantom.common.util.NetUtils;
import com.phantom.dispatcher.acceptor.AcceptorServerManager;
import com.phantom.dispatcher.message.MessageHandler;
import com.phantom.dispatcher.message.MessageHandlerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 分发服务器
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 18:05
 */
@Slf4j
public class DispatcherHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String instanceId = NetUtils.getChannelId(channel);
        log.info("与接入系统建立连接: {}", instanceId);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String channelId = NetUtils.getChannelId((SocketChannel) ctx.channel());
        log.info("与接入系统断开连接: {}", channelId);
        AcceptorServerManager.getInstance().removeAcceptorInstance(channelId);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf byteBuf = (ByteBuf) msg;
            Message message = Message.parse(byteBuf);
            MessageHandler messageHandler = MessageHandlerFactory.getMessageHandler(message.getRequestType());
            if (messageHandler != null) {
                messageHandler.handleMessage(message, (SocketChannel) ctx.channel());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("发生异常：{}", cause.getMessage());
        cause.printStackTrace();
    }
}
