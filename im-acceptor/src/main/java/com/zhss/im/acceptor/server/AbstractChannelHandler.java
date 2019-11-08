package com.zhss.im.acceptor.server;

import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.message.MessageHandler;
import com.zhss.im.acceptor.message.MessageHandlerFactory;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * 默认的消息处理器，封装了消息处理的流程
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 15:56
 */
public abstract class AbstractChannelHandler extends ChannelInboundHandlerAdapter {

    protected DispatcherManager dispatcherManager;
    protected SessionManagerFacade sessionManagerFacade;

    public AbstractChannelHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        this.dispatcherManager = dispatcherManager;
        this.sessionManagerFacade = sessionManagerFacade;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        Message message = Message.parse(byteBuf);
        MessageHandler messageHandler = MessageHandlerFactory.getMessageHandler(message.getRequestType(),
                dispatcherManager, sessionManagerFacade);
        if (messageHandler != null) {
            SocketChannel channel = (SocketChannel) ctx.channel();
            messageHandler.handleMessage(message, channel);
        }
    }
}
