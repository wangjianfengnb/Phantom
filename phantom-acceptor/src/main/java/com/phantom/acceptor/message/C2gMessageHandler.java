package com.phantom.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManager;
import com.phantom.common.C2gMessageRequest;
import com.phantom.common.C2gMessageResponse;
import com.phantom.common.Constants;
import com.phantom.common.Message;
import com.phantom.common.model.wrapper.C2gMessageRequestWrapper;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 处理群聊消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 17:08
 */
public class C2gMessageHandler extends AbstractMessageHandler<C2gMessageRequestWrapper> {

    C2gMessageHandler(DispatcherManager dispatcherManager, SessionManager sessionManager,
                      ThreadPoolExecutor threadPoolExecutor) {
        super(dispatcherManager, sessionManager, threadPoolExecutor);
    }

    @Override
    protected C2gMessageRequestWrapper parseMessage(Message message) throws InvalidProtocolBufferException {
        C2gMessageRequest c2gMessageRequest = C2gMessageRequest.parseFrom(message.getBody());
        return C2gMessageRequestWrapper.builder()
                .c2gMessageRequest(c2gMessageRequest)
                .build();
    }

    @Override
    protected String getReceiverId(C2gMessageRequestWrapper message) {
        return message.getC2gMessageRequest().getGroupId();
    }

    @Override
    protected String getResponseUid(Message message) throws InvalidProtocolBufferException {
        C2gMessageRequest response = C2gMessageRequest.parseFrom(message.getBody());
        return response.getSenderId();
    }

    @Override
    protected Message getErrorResponse(C2gMessageRequestWrapper message) {
        C2gMessageRequest c2gMessageRequest = message.getC2gMessageRequest();
        C2gMessageResponse response = C2gMessageResponse.newBuilder()
                .setSenderId(c2gMessageRequest.getSenderId())
                .setGroupId(c2gMessageRequest.getGroupId())
                .setTimestamp(c2gMessageRequest.getTimestamp())
                .setStatus(Constants.RESPONSE_STATUS_ERROR)
                .build();
        return Message.buildC2gMessageResponse(response);
    }
}
