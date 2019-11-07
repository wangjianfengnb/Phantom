package com.zhss.im.acceptor.dispatcher;

import com.zhss.im.acceptor.ClientManager;
import com.zhss.im.protocol.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 与分发系统建立连接
 *
 * @author Jianfeng Wang
 * @since 2019/11/7 15:56
 */
@Slf4j
public class DispatcherHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String instanceId = NetUtils.getInstanceId(channel);
        DispatcherInstance instance = DispatcherInstance.builder()
                .channel(channel)
                .build();
        log.info("接入系统和分发系统连接建立: {}", instanceId);
        DispatcherManager.getInstance().addDispatcherInstance(instanceId, instance);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String instanceId = NetUtils.getInstanceId((SocketChannel) ctx.channel());
        log.info("接入系统和分发系统连接断开: {}", instanceId);
        DispatcherManager.getInstance().removeDispatcherInstance(instanceId);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("接入系统收到分发系统的消息：{}", msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        Message message = Message.parse(byteBuf);
        int requestType = message.getRequestType();
        if (Constants.REQUEST_TYPE_AUTHENTICATE == requestType) {
            byte[] body = message.getBody();
            AuthenticateResponseProto.AuthenticateResponse authenticateResponse =
                    AuthenticateResponseProto.AuthenticateResponse.parseFrom(body);
            String uid = authenticateResponse.getUid();
            SocketChannel client = ClientManager.getInstance().getClient(uid);
            client.writeAndFlush(message.getBuffer());
        }
    }
}
