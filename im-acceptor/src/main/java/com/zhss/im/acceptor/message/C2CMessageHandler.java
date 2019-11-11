package com.zhss.im.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.protocol.*;

/**
 * 处理C2C消息
 * <p>
 * 对于C2C消息来说，什么事情都不用干，请求推送给分发系统，响应推送给客户端
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 16:50
 */
public class C2CMessageHandler extends AbstractMessageHandler {

    C2CMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        super(dispatcherManager, sessionManagerFacade);
    }

    @Override
    protected String getUid(Message message, int messageType) throws InvalidProtocolBufferException {
        byte[] body = message.getBody();
        if (messageType == Constants.MESSAGE_TYPE_REQUEST) {
            C2CMessageRequest c2CMessageRequest = C2CMessageRequest.parseFrom(body);
            return c2CMessageRequest.getSenderId();
        } else {
            C2CMessageResponse c2CMessageResponse = C2CMessageResponse.parseFrom(body);
            return c2CMessageResponse.getSenderId();
        }
    }

    @Override
    protected void handleResponseMessage(String uid, Message message) {

    }

}
