package com.zhss.im.gateway;

import com.zhss.im.protocol.AuthenticateRequestProto;
import com.zhss.im.protocol.AuthenticateResponseProto;
import com.zhss.im.protocol.Constants;
import com.zhss.im.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

/**
 * 处理连接
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:50
 */
public class AcceptorHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected from :" + ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("收到消息");
        ByteBuf byteBuf = (ByteBuf) msg;
        Message message = Message.parse(byteBuf);
        int requestType = message.getRequestType();
        if (Constants.REQUEST_TYPE_AUTHENTICATE == requestType) {
            System.out.println("收到认证请求");
            byte[] body = message.getBody();
            AuthenticateRequestProto.AuthenticateRequest authenticateRequest =
                    AuthenticateRequestProto.AuthenticateRequest.parseFrom(body);
            AuthenticateResponseProto.AuthenticateResponse response =
                    AuthenticateResponseProto.AuthenticateResponse.newBuilder()
                            .setToken(authenticateRequest.getToken())
                            .setUid(authenticateRequest.getUid())
                            .setTimestamp(System.currentTimeMillis())
                            .build();
            Message resp = Message.buildAuthenticateResponse(response);
            ctx.channel().writeAndFlush(resp.getBuffer());
            ClientManager.getInstance().addClient(authenticateRequest.getUid(), (SocketChannel) ctx.channel());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
