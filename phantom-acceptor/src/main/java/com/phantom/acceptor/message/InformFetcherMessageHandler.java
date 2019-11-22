package com.phantom.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManagerFacade;
import com.phantom.common.InformFetchMessageResponse;
import com.phantom.common.Message;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 通知客户端抓取离线消息处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/18 14:58
 */
@Slf4j
public class InformFetcherMessageHandler extends AbstractMessageHandler {

    InformFetcherMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade,
                                ThreadPoolExecutor threadPoolExecutor) {
        super(dispatcherManager, sessionManagerFacade, threadPoolExecutor);
    }

    @Override
    protected String getReceiverId(Message message) throws InvalidProtocolBufferException {
        throw new IllegalArgumentException("通知消息不能由客户端主动发送");
    }

    @Override
    protected String getResponseUid(Message message) throws InvalidProtocolBufferException {
        InformFetchMessageResponse informFetchMessageResponse = InformFetchMessageResponse.parseFrom(message.getBody());
        return informFetchMessageResponse.getUid();
    }

    @Override
    protected Message getErrorResponse(Message message) throws InvalidProtocolBufferException {
        throw new IllegalArgumentException("通知消息不能由客户端主动发送");
    }
}
