package com.zhss.im.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.common.Message;
import com.zhss.im.protocol.C2CMessageRequest;
import com.zhss.im.protocol.C2CMessageResponse;

/**
 * 处理C2C消息
 * <p>
 * 对于C2C消息来说，什么事情都不用干，请求推送给分发系统，响应推送给客户端
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 16:50
 */
public class C2cMessageHandler extends AbstractMessageHandler {

    C2cMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        super(dispatcherManager, sessionManagerFacade);
    }

    @Override
    protected String getReceiverId(Message message) throws InvalidProtocolBufferException {
        C2CMessageRequest c2CMessageRequest = C2CMessageRequest.parseFrom(message.getBody());
        return c2CMessageRequest.getReceiverId();
    }

    @Override
    protected String getResponseUid(Message message) throws InvalidProtocolBufferException {
        C2CMessageResponse c2CMessageResponse = C2CMessageResponse.parseFrom(message.getBody());
        return c2CMessageResponse.getSenderId();
    }

}
