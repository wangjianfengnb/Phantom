package com.zhss.im.client.interceptor;

import com.zhss.im.protocol.Message;

/**
 * 消息拦截器
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 16:35
 */
public interface MessageInterceptor {

    /**
     * 发送消息之前
     *
     * @param message 消息
     */
    void beforeSend(Message message);

    /**
     * 发送消息完成
     *
     * @param message 消息
     */
    void afterSend(Message message);

}
