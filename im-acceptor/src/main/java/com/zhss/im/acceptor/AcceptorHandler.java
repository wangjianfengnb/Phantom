package com.zhss.im.acceptor;

import com.zhss.im.acceptor.dispatcher.DispatcherInstance;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.protocol.AuthenticateRequestProto;
import com.zhss.im.protocol.Constants;
import com.zhss.im.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理连接
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:50
 */
@Slf4j
public class AcceptorHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端建立连接 : {}", ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("收到客户端消息：{}", msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        Message message = Message.parse(byteBuf);
        int requestType = message.getRequestType();
        if (Constants.REQUEST_TYPE_AUTHENTICATE == requestType) {
            byte[] body = message.getBody();
            AuthenticateRequestProto.AuthenticateRequest authenticateRequest =
                    AuthenticateRequestProto.AuthenticateRequest.parseFrom(body);
            ClientManager.getInstance().addClient(authenticateRequest.getUid(),
                    (SocketChannel) ctx.channel());
        }
        log.info("将消息发送到分发系统");
        DispatcherInstance dispatcherInstance = DispatcherManager.getInstance().chooseDispatcher();
        dispatcherInstance.sendMsg(message.getBuffer());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ClientManager.getInstance().removeClient((SocketChannel) ctx.channel());
    }

}
