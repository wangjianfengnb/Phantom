package com.zhss.im.dispatcher.server;

import com.zhss.im.dispatcher.acceptor.AcceptorInstance;
import com.zhss.im.dispatcher.acceptor.AcceptorServerManager;
import com.zhss.im.dispatcher.config.DispatcherConfig;
import com.zhss.im.dispatcher.message.MessageHandler;
import com.zhss.im.dispatcher.message.MessageHandlerFactory;
import com.zhss.im.dispatcher.session.SessionManager;
import com.zhss.im.protocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 分发服务器
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 18:05
 */
@Slf4j
public class DispatcherHandler extends ChannelInboundHandlerAdapter {

    private SessionManager sessionManager;

    public DispatcherHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String instanceId = NetUtils.getInstanceId(channel);
        AcceptorInstance instance = AcceptorInstance.builder()
                .channel(channel)
                .build();
        log.info("接入系统建立连接: {}", instanceId);
        AcceptorServerManager.getInstance().addAcceptorInstance(instanceId, instance);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String instanceId = NetUtils.getInstanceId((SocketChannel) ctx.channel());
        log.info("接入系统断开连接: {}", instanceId);
        AcceptorServerManager.getInstance().removeAcceptorInstance(instanceId);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        Message message = Message.parse(byteBuf);
        MessageHandler messageHandler = MessageHandlerFactory.getMessageHandler(message.getRequestType(),
                sessionManager);
        if (messageHandler != null) {
            messageHandler.handleMessage(message, (SocketChannel) ctx.channel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
