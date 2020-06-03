package com.phantom.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManager;
import com.phantom.common.C2cMessageRequest;
import com.phantom.common.C2cMessageResponse;
import com.phantom.common.Constants;
import com.phantom.common.Message;
import com.phantom.common.model.wrapper.C2cMessageRequestWrapper;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 处理C2c消息
 * <p>
 * 对于C2c消息来说，什么事情都不用干，请求推送给分发系统，响应推送给客户端
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 16:50
 */
public class C2cMessageHandler extends AbstractMessageHandler<C2cMessageRequestWrapper> {

    C2cMessageHandler(DispatcherManager dispatcherManager, SessionManager sessionManager,
                      ThreadPoolExecutor threadPoolExecutor) {
        super(dispatcherManager, sessionManager, threadPoolExecutor);
    }

    @Override
    protected C2cMessageRequestWrapper parseMessage(Message message) throws InvalidProtocolBufferException {
        C2cMessageRequest c2cMessageRequest = C2cMessageRequest.parseFrom(message.getBody());
        return C2cMessageRequestWrapper.create(c2cMessageRequest);
    }

    @Override
    protected String getReceiverId(C2cMessageRequestWrapper message)  {
        C2cMessageRequest c2cMessageRequest = message.getC2cMessageRequest();
        return c2cMessageRequest.getReceiverId();
    }

    @Override
    protected String getResponseUid(Message message) throws InvalidProtocolBufferException {
        C2cMessageResponse c2cMessageResponse = C2cMessageResponse.parseFrom(message.getBody());
        return c2cMessageResponse.getSenderId();
    }

    @Override
    protected Message getErrorResponse(C2cMessageRequestWrapper message) {
        C2cMessageRequest c2cMessageRequest = message.getC2cMessageRequest();
        C2cMessageResponse response = C2cMessageResponse.newBuilder()
                .setTimestamp(System.currentTimeMillis())
                .setStatus(Constants.RESPONSE_STATUS_ERROR)
                .setReceiverId(c2cMessageRequest.getReceiverId())
                .setSenderId(c2cMessageRequest.getSenderId())
                .build();
        return Message.buildC2cMessageResponse(response);
    }
}
