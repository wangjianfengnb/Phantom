package com.phantom.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManagerFacade;
import com.phantom.common.C2CMessageRequest;
import com.phantom.common.C2CMessageResponse;
import com.phantom.common.Constants;
import com.phantom.common.Message;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 处理C2C消息
 * <p>
 * 对于C2C消息来说，什么事情都不用干，请求推送给分发系统，响应推送给客户端
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 16:50
 */
public class C2cMessageHandler extends AbstractMessageHandler {

    C2cMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade,
                      ThreadPoolExecutor threadPoolExecutor) {
        super(dispatcherManager, sessionManagerFacade, threadPoolExecutor);
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

    @Override
    protected Message getErrorResponse(Message message) throws InvalidProtocolBufferException {
        C2CMessageRequest c2CMessageRequest = C2CMessageRequest.parseFrom(message.getBody());
        C2CMessageResponse response = C2CMessageResponse.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setStatus(Constants.RESPONSE_STATUS_ERROR)
                .setReceiverId(c2CMessageRequest.getReceiverId())
                .setSenderId(c2CMessageRequest.getSenderId())
                .build();
        return Message.buildC2cMessageResponse(response);
    }

}
