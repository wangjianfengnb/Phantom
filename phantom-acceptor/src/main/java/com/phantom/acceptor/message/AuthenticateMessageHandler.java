package com.phantom.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManagerFacade;
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
public class AuthenticateMessageHandler extends AbstractMessageHandler {

    AuthenticateMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade,
                               ThreadPoolExecutor threadPoolExecutor) {
        super(dispatcherManager, sessionManagerFacade, threadPoolExecutor);
    }

    @Override
    protected String getReceiverId(Message message) throws InvalidProtocolBufferException {
        AuthenticateRequest authenticateRequest = AuthenticateRequest.parseFrom(message.getBody());
        return authenticateRequest.getUid();
    }

    @Override
    protected String getResponseUid(Message message) throws InvalidProtocolBufferException {
        AuthenticateResponse authenticateResponse = AuthenticateResponse.parseFrom(message.getBody());
        return authenticateResponse.getUid();
    }

    @Override
    protected Message getErrorResponse(Message message) throws InvalidProtocolBufferException {
        AuthenticateRequest authenticateRequest = AuthenticateRequest.parseFrom(message.getBody());
        AuthenticateResponse response = AuthenticateResponse.newBuilder()
                .setToken(authenticateRequest.getToken())
                .setUid(authenticateRequest.getUid())
                .setTimestamp(System.currentTimeMillis())
                .setStatus(Constants.RESPONSE_STATUS_ERROR)
                .build();
        return Message.buildAuthenticateResponse(response);
    }

    @Override
    protected void beforeDispatchMessage(String uid, Message message, SocketChannel channel) {
        // 认证的时候，在转发到分发系统前需要保存和客户端的连接
        sessionManagerFacade.addSession(uid, channel);
    }
}
