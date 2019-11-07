package com.zhss.im.dispatcher;

import com.zhss.im.dispatcher.acceptor.AcceptorInstance;
import com.zhss.im.dispatcher.acceptor.AcceptorServerManager;
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
        int requestType = message.getRequestType();
        if (Constants.REQUEST_TYPE_AUTHENTICATE == requestType) {
            log.info("收到认证请求.....");
            byte[] body = message.getBody();
            AuthenticateRequestProto.AuthenticateRequest authenticateRequest =
                    AuthenticateRequestProto.AuthenticateRequest.parseFrom(body);
            AuthenticateResponseProto.AuthenticateResponse response =
                    AuthenticateResponseProto.AuthenticateResponse.newBuilder()
                            .setToken(authenticateRequest.getToken())
                            .setUid(authenticateRequest.getUid())
                            .setTimestamp(System.currentTimeMillis())
                            .setStatus(Constants.RESPONSE_STATUS_OK)
                            .build();
            Message resp = Message.buildAuthenticateResponse(response);
            ctx.channel().writeAndFlush(resp.getBuffer());
        }
    }


}
