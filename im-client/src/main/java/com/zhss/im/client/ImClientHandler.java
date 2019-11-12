package com.zhss.im.client;

import com.zhss.im.common.AuthenticateResponse;
import com.zhss.im.common.C2CMessageResponse;
import com.zhss.im.common.Constants;
import com.zhss.im.common.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
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
        ByteBuf byteBuf = (ByteBuf) msg;
        Message message = Message.parse(byteBuf);
        int requestType = message.getRequestType();
        if (Constants.REQUEST_TYPE_AUTHENTICATE == requestType) {
            byte[] body = message.getBody();
            AuthenticateResponse authenticateResponse =
                    AuthenticateResponse.parseFrom(body);
            if (authenticateResponse.getStatus() == Constants.RESPONSE_STATUS_OK) {
                log.info("认证请求成功...");
                ConnectionManager.getInstance().setAuthenticate(true);
            } else {
                log.info("认证请求失败...");
                ctx.close();
            }
        } else if (Constants.REQUEST_TYPE_C2C_SEND == requestType) {
            byte[] body = message.getBody();
            C2CMessageResponse c2CMessageResponse =
                    C2CMessageResponse.parseFrom(body);
            if (c2CMessageResponse.getStatus() == Constants.RESPONSE_STATUS_OK) {
                log.info("发送单聊消息成功...");
            } else {
                log.info("发送单聊消息失败，重新发送...");
            }
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接断开：{}", ctx);
        ConnectionManager.getInstance().shutdown();
    }
}
