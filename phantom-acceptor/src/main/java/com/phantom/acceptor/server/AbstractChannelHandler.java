package com.phantom.acceptor.server;

import com.phantom.acceptor.message.MessageHandler;
import com.phantom.acceptor.message.MessageHandlerFactory;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManager;
import com.phantom.common.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的消息处理器，封装了消息处理的流程
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 15:56
 */
@Slf4j
public abstract class AbstractChannelHandler extends ChannelInboundHandlerAdapter {

    protected DispatcherManager dispatcherManager;
    protected SessionManager sessionManager;

    public AbstractChannelHandler(DispatcherManager dispatcherManager, SessionManager sessionManager) {
        this.dispatcherManager = dispatcherManager;
        this.sessionManager = sessionManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf byteBuf = (ByteBuf) msg;
            Message message = Message.parse(byteBuf);
            MessageHandler messageHandler = MessageHandlerFactory.getMessageHandler(message.getRequestType());
            if (messageHandler != null) {
                SocketChannel channel = (SocketChannel) ctx.channel();
                messageHandler.handleMessage(message, channel);
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
