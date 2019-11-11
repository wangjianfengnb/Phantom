package com.zhss.im.dispatcher.message;

import com.zhss.im.dispatcher.session.Session;
import com.zhss.im.dispatcher.session.SessionManager;
import com.zhss.im.dispatcher.sso.Authenticator;
import com.zhss.im.dispatcher.sso.DefaultAuthenticator;
import com.zhss.im.protocol.*;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 认证消息处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 10:54
 */
@Slf4j
public class AuthenticateMessageHandler extends AbstractMessageHandler {

    /**
     * 请求认证器
     */
    private Authenticator authenticator;

    AuthenticateMessageHandler(SessionManager sessionManager) {
        super(sessionManager);
        this.authenticator = new DefaultAuthenticator();
    }

    @Override
    public void handleMessage(Message message, SocketChannel channel) throws Exception {
        log.info("收到认证请求.....");
        byte[] body = message.getBody();
        AuthenticateRequest authenticateRequest =
                AuthenticateRequest.parseFrom(body);
        String uid = authenticateRequest.getUid();
        String token = authenticateRequest.getToken();
        execute(uid, () -> {
            // 认证发送响应
            AuthenticateResponse.Builder responseBuilder = AuthenticateResponse.newBuilder()
                    .setToken(authenticateRequest.getToken())
                    .setUid(authenticateRequest.getUid())
                    .setTimestamp(System.currentTimeMillis());
            if (authenticator.authenticate(uid, token)) {
                responseBuilder.setStatus(Constants.RESPONSE_STATUS_OK);
                log.info("认证请求成功");
                Session session = Session.builder()
                        .uid(uid)
                        .token(token)
                        .acceptorChannelId(NetUtils.getInstanceId(channel))
                        .timestamp(System.currentTimeMillis())
                        .build();
                sessionManager.addSession(uid, session);
            } else {
                responseBuilder.setStatus(Constants.RESPONSE_STATUS_ERROR);
                log.info("认证请求失败");
            }
            Message response = Message.buildAuthenticateResponse(responseBuilder.build());
            channel.writeAndFlush(response.getBuffer());
        });
    }
}
