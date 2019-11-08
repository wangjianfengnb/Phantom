package com.zhss.im.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.protocol.AuthenticateRequest;
import com.zhss.im.protocol.AuthenticateResponse;
import com.zhss.im.protocol.Constants;
import com.zhss.im.protocol.Message;
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

    private SessionManagerFacade sessionManagerFacade;

    public AuthenticateMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        super(dispatcherManager);
        this.sessionManagerFacade = sessionManagerFacade;
    }

    @Override
    public void handleMessage(Message message, SocketChannel channel) throws Exception {
        switch (message.getMessageType()) {
            case Constants.MESSAGE_TYPE_REQUEST:
                handleRequestMessage(message, channel);
                break;
            case Constants.MESSAGE_TYPE_RESPONSE:
                handleResponseMessage(message);
                break;
            default:
                break; // no-op
        }
    }

    /**
     * 处理认证响应
     *
     * @param message 消息
     */
    private void handleResponseMessage(Message message) throws InvalidProtocolBufferException {
        byte[] body = message.getBody();
        AuthenticateResponse authenticateResponse = AuthenticateResponse.parseFrom(body);
        String uid = authenticateResponse.getUid();
        SocketChannel session = sessionManagerFacade.getSession(uid);
        if (session != null) {
            // 有可能在分发系统发到接入系统的过程中，刚好客户端断线了
            session.writeAndFlush(message.getBuffer());
        }
    }

    /**
     * 处理认证请求
     *
     * @param message 消息
     * @param channel 渠道
     */
    private void handleRequestMessage(Message message, SocketChannel channel) throws InvalidProtocolBufferException {
        byte[] body = message.getBody();
        AuthenticateRequest authenticateRequest = AuthenticateRequest.parseFrom(body);
        sessionManagerFacade.addSession(authenticateRequest.getUid(), channel);
        sendMessage(message);
    }
}
