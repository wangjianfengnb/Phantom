package com.zhss.im.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.common.C2GMessageRequest;
import com.zhss.im.common.C2GMessageResponse;
import com.zhss.im.common.Message;

/**
 * 处理群聊消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 17:08
 */
public class C2gMessageHandler extends AbstractMessageHandler {

    C2gMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        super(dispatcherManager, sessionManagerFacade);
    }

    @Override
    protected String getReceiverId(Message message) throws InvalidProtocolBufferException {
        C2GMessageRequest c2GMessageRequest = C2GMessageRequest.parseFrom(message.getBody());
        return c2GMessageRequest.getGroupId();
    }

    @Override
    protected String getResponseUid(Message message) throws InvalidProtocolBufferException {
        C2GMessageResponse response = C2GMessageResponse.parseFrom(message.getBody());
        return response.getSenderId();
    }
}
