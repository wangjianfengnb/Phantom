package com.zhss.im.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.common.AuthenticateRequest;
import com.zhss.im.common.AuthenticateResponse;
import com.zhss.im.common.Message;
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

    AuthenticateMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        super(dispatcherManager, sessionManagerFacade);
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
    protected void beforeDispatchMessage(String uid, Message message, SocketChannel channel) {
        // 认证的时候，在转发到分发系统前需要保存和客户端的连接
        sessionManagerFacade.addSession(uid, channel);
    }
}
