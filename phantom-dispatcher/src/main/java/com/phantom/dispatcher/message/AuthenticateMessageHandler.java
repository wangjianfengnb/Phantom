package com.phantom.dispatcher.message;

import com.phantom.common.*;
import com.phantom.common.util.NetUtils;
import com.phantom.dispatcher.acceptor.AcceptorServerManager;
import com.phantom.dispatcher.session.Session;
import com.phantom.dispatcher.session.SessionManager;
import com.phantom.dispatcher.sso.Authenticator;
import com.phantom.dispatcher.sso.DefaultAuthenticator;
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
        byte[] body = message.getBody();
        AuthenticateRequest authenticateRequest = AuthenticateRequest.parseFrom(body);
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
                AcceptorServerManager acceptorServerManager = AcceptorServerManager.getInstance();
                String acceptorInstanceId = acceptorServerManager.getAcceptorInstanceId(NetUtils.getChannelId(channel));
                Session session = Session.builder()
                        .uid(uid)
                        .token(token)
                        .acceptorInstanceId(acceptorInstanceId)
                        .timestamp(System.currentTimeMillis())
                        .build();
                sessionManager.addSession(uid, session);
            } else {
                responseBuilder.setStatus(Constants.RESPONSE_STATUS_ERROR);
            }
            Message response = Message.buildAuthenticateResponse(responseBuilder.build());
            channel.writeAndFlush(response.getBuffer());
        });
    }
}
