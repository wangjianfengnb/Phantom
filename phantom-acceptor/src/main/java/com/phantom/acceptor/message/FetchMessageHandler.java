package com.phantom.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManager;
import com.phantom.common.FetchMessageRequest;
import com.phantom.common.FetchMessageResponse;
import com.phantom.common.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 抓取消息请求处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/18 15:13
 */
@Slf4j
public class FetchMessageHandler extends AbstractMessageHandler<FetchMessageRequest> {

    FetchMessageHandler(DispatcherManager dispatcherManager, SessionManager sessionManager,
                        ThreadPoolExecutor threadPoolExecutor) {
        super(dispatcherManager, sessionManager, threadPoolExecutor);
    }

    @Override
    protected FetchMessageRequest parseMessage(Message message) throws InvalidProtocolBufferException {
        return FetchMessageRequest.parseFrom(message.getBody());
    }

    @Override
    protected String getReceiverId(FetchMessageRequest message) {
        return message.getUid();
    }

    @Override
    protected String getResponseUid(Message message) throws InvalidProtocolBufferException {
        FetchMessageResponse fetchMessageResponse = FetchMessageResponse.parseFrom(message.getBody());
        return fetchMessageResponse.getUid();
    }

    @Override
    protected Message getErrorResponse(FetchMessageRequest message) {
        FetchMessageResponse response = FetchMessageResponse.newBuilder()
                .setIsEmpty(true)
                .setUid(message.getUid())
                .build();
        return Message.buildFetcherMessageResponse(response);
    }
}
