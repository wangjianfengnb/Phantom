package com.zhss.im.acceptor.message;

import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.protocol.Constants;

/**
 * 消息处理器工厂
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 10:55
 */
public class MessageHandlerFactory {

    /**
     * 根据请求类型获取消息处理器
     *
     * @param requestType 请求类型
     * @return 消息处理器
     */
    public static MessageHandler getMessageHandler(int requestType, DispatcherManager dispatcherManager,
                                                   SessionManagerFacade sessionManagerFacade) {
        if (requestType == Constants.REQUEST_TYPE_AUTHENTICATE) {
            return new AuthenticateMessageHandler(dispatcherManager, sessionManagerFacade);
        }
        return null;
    }


}
