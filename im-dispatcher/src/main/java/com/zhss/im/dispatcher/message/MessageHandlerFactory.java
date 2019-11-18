package com.zhss.im.dispatcher.message;

import com.zhss.im.common.Constants;
import com.zhss.im.dispatcher.session.SessionManager;

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
        handlers.put(Constants.REQUEST_TYPE_AUTHENTICATE, new AuthenticateMessageHandler(sessionManager));
        handlers.put(Constants.REQUEST_TYPE_C2C_SEND, new C2cMessageHandler(sessionManager));
        handlers.put(Constants.REQUEST_TYPE_MESSAGE_FETCH, new PushMessageHandler(sessionManager));


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
