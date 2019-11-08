package com.zhss.im.acceptor.message;

import com.zhss.im.acceptor.dispatcher.DispatcherInstance;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.protocol.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * 公共消息处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 16:02
 */
@Slf4j
public abstract class AbstractMessageHandler implements MessageHandler {

    private DispatcherManager dispatcherManager;

    public AbstractMessageHandler(DispatcherManager dispatcherManager) {
        this.dispatcherManager = dispatcherManager;
    }

    /**
     * 发送消息到分发系统
     *
     * @param message 消息
     */
    protected void sendMessage(Message message) {
        log.info("将消息发送到分发系统...");
        DispatcherInstance dispatcherInstance = dispatcherManager.chooseDispatcher();
        dispatcherInstance.sendMessage(message.getBuffer());
    }

}
