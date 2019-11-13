package com.zhss.im.dispatcher.mq;

/**
 * 消息监听器
 *
 * @author Jianfeng Wang
 * @since 2019/11/13 10:54
 */
public interface MessageListener {

    /**
     * 收到消息
     *
     * @param message 消息
     */
    void onMessage(String message);

}
