package com.zhss.im.dispatcher.message;

import com.zhss.im.dispatcher.session.SessionManager;
import com.zhss.im.protocol.Constants;

/**
 * 消息处理器工厂
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 10:55
 */
public class MessageHandlerFactory {

    private AuthenticateMessageHandler authenticateMessageHandler;

    private C2CMessageHandler c2CMessageHandler;

    /**
     * 根据请求类型获取消息处理器
     *
     * @param requestType 请求类型
     * @return 消息处理器
     */
    public static MessageHandler getMessageHandler(int requestType, SessionManager sessionManager) {
        if (requestType == Constants.REQUEST_TYPE_AUTHENTICATE) {
            return new AuthenticateMessageHandler(sessionManager);
        } else if (requestType == Constants.REQUEST_TYPE_C2C_SEND) {
            return new C2CMessageHandler(sessionManager);
        }
        return null;
    }


}
