package com.zhss.im.acceptor.message;

import com.zhss.im.common.Message;
import io.netty.channel.socket.SocketChannel;

/**
 * 消息处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 10:49
 */
public interface MessageHandler {


    /**
     * 处理消息
     *
     * @param message 消息
     * @param channel channel
     * @throws Exception 序列化失败
     */
    void handleMessage(Message message, SocketChannel channel) throws Exception;


}
