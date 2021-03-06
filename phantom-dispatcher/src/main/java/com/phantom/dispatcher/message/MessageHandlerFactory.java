package com.phantom.dispatcher.message;

import com.phantom.dispatcher.session.SessionManager;
import com.phantom.common.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息处理器工厂
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 10:55
 */
public class MessageHandlerFactory {

    /**
     * 处理消息的handler集合
     */
    private static Map<Integer, MessageHandler> handlers = new ConcurrentHashMap<>();

    /**
     * 初始化
     *
     * @param sessionManager 回话管理器
     */
    public static void initialize(SessionManager sessionManager) {
        handlers.put(Constants.REQUEST_TYPE_C2C_SEND, new C2cMessageHandler(sessionManager));
        handlers.put(Constants.REQUEST_TYPE_MESSAGE_FETCH, new PushMessageHandler(sessionManager));
        handlers.put(Constants.REQUEST_TYPE_C2G_SEND, new C2gMessageHandler(sessionManager));
        handlers.put(Constants.REQUEST_TYPE_ACCEPTOR_REGISTER, new AcceptorRegisterMessageHandler(sessionManager));
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
