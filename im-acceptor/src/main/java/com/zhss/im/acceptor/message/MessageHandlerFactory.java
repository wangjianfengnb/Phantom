package com.zhss.im.acceptor.message;

import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.common.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息处理器工厂
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 10:55
 */
public class MessageHandlerFactory {


    private static Map<Integer, MessageHandler> handlers = new HashMap<>();

    public static void initialize(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        handlers.put(Constants.REQUEST_TYPE_AUTHENTICATE, new AuthenticateMessageHandler(dispatcherManager, sessionManagerFacade));
        handlers.put(Constants.REQUEST_TYPE_C2C_SEND, new C2cMessageHandler(dispatcherManager, sessionManagerFacade));
        handlers.put(Constants.REQUEST_TYPE_INFORM_FETCH, new InformFetcherMessageHandler(dispatcherManager, sessionManagerFacade));
        handlers.put(Constants.REQUEST_TYPE_MESSAGE_FETCH, new FetchMessageHandler(dispatcherManager, sessionManagerFacade));
        handlers.put(Constants.REQUEST_TYPE_C2G_SEND, new C2gMessageHandler(dispatcherManager, sessionManagerFacade));
    }


    /**
     * 根据请求类型获取消息处理器
     *
     * @param requestType 请求类型
     * @return 消息处理器
     */
    public static MessageHandler getMessageHandler(int requestType) {
        return handlers.get(requestType);
    }


}
