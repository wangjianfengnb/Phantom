package com.phantom.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManagerFacade;
import com.phantom.common.C2GMessageRequest;
import com.phantom.common.C2GMessageResponse;
import com.phantom.common.Constants;
import com.phantom.common.Message;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 处理群聊消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 17:08
 */
public class C2gMessageHandler extends AbstractMessageHandler {

    C2gMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade,
                      ThreadPoolExecutor threadPoolExecutor) {
        super(dispatcherManager, sessionManagerFacade, threadPoolExecutor);
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

    @Override
    protected Message getErrorResponse(Message message) throws InvalidProtocolBufferException {
        C2GMessageRequest c2GMessageRequest = C2GMessageRequest.parseFrom(message.getBody());
        C2GMessageResponse response = C2GMessageResponse.newBuilder()
                .setSenderId(c2GMessageRequest.getSenderId())
                .setGroupId(c2GMessageRequest.getGroupId())
                .setTimestamp(c2GMessageRequest.getTimestamp())
                .setStatus(Constants.RESPONSE_STATUS_ERROR)
                .build();
        return Message.buildC2gMessageResponse(response);
    }
}
