package com.phantom.acceptor.message;

import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.Session;
import com.phantom.acceptor.session.SessionManager;
import com.phantom.acceptor.sso.Authenticator;
import com.phantom.acceptor.sso.DefaultAuthenticator;
import com.phantom.acceptor.zookeeper.ZookeeperManager;
import com.phantom.common.AuthenticateRequest;
import com.phantom.common.AuthenticateResponse;
import com.phantom.common.Constants;
import com.phantom.common.Message;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 认证消息处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 10:54
 */
@Slf4j
public class AuthenticateMessageHandler implements MessageHandler {

    private ZookeeperManager zookeeperManager;
    private Authenticator authenticator;
    protected DispatcherManager dispatcherManager;
    protected SessionManager sessionManager;
    protected ThreadPoolExecutor threadPoolExecutor;

    AuthenticateMessageHandler(DispatcherManager dispatcherManager, SessionManager sessionManager,
                               ThreadPoolExecutor threadPoolExecutor, ZookeeperManager zookeeperManager) {
        this.dispatcherManager = dispatcherManager;
        this.sessionManager = sessionManager;
        this.threadPoolExecutor = threadPoolExecutor;
        this.zookeeperManager = zookeeperManager;
        this.authenticator = new DefaultAuthenticator();
    }

    @Override
    public void handleMessage(Message message, SocketChannel channel) {
        threadPoolExecutor.execute(() -> {
            try {
                byte[] body = message.getBody();
                AuthenticateRequest authenticateRequest = AuthenticateRequest.parseFrom(body);
                String uid = authenticateRequest.getUid();
                String token = authenticateRequest.getToken();
                // 认证发送响应
                AuthenticateResponse.Builder responseBuilder = AuthenticateResponse.newBuilder()
                        .setToken(authenticateRequest.getToken())
                        .setUid(authenticateRequest.getUid())
                        .setTimestamp(System.currentTimeMillis());
                if (authenticator.authenticate(uid, token)) {
                    responseBuilder.setStatus(Constants.RESPONSE_STATUS_OK);
                    String acceptorInstanceId = dispatcherManager.getAcceptorInstanceId();
                    Session session = Session.builder()
                            .uid(uid)
                            .token(token)
                            .acceptorInstanceId(acceptorInstanceId)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    sessionManager.addSession(uid, session, channel);
                    zookeeperManager.incrementClient();
                } else {
                    responseBuilder.setStatus(Constants.RESPONSE_STATUS_ERROR);
                }
                Message response = Message.buildAuthenticateResponse(responseBuilder.build());
                channel.writeAndFlush(response.getBuffer());
            } catch (Exception e) {
                log.error("处理认证消息出错：", e);
            }
        });
    }
}
