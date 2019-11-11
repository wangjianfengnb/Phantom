package com.zhss.im.acceptor.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.zhss.im.acceptor.dispatcher.DispatcherInstance;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.protocol.Constants;
import com.zhss.im.protocol.Message;
import io.netty.channel.socket.SocketChannel;
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

    protected SessionManagerFacade sessionManagerFacade;

    AbstractMessageHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        this.dispatcherManager = dispatcherManager;
        this.sessionManagerFacade = sessionManagerFacade;
    }

    @Override
    public void handleMessage(Message message, SocketChannel channel) throws Exception {
        String uid = getUid(message, message.getMessageType());
        switch (message.getMessageType()) {
            case Constants.MESSAGE_TYPE_REQUEST:
                handleRequestMessage(uid, message, channel);
                break;
            case Constants.MESSAGE_TYPE_RESPONSE:
                handleResponseMessage(uid, message);
                break;
            default:
                break; // no-op
        }
    }

    /**
     * 获取用户ID
     *
     * @param message     消息
     * @param messageType 消息类型 {@link Constants#MESSAGE_TYPE_REQUEST}
     * @return 用户Id
     * @throws InvalidProtocolBufferException Protobuf序列化失败
     */
    protected abstract String getUid(Message message, int messageType) throws InvalidProtocolBufferException;


    /**
     * 处理响应消息,返回给客户端
     *
     * @param uid     用户ID
     * @param message 消息
     */
    protected void handleResponseMessage(String uid, Message message) {
        SocketChannel session = sessionManagerFacade.getSession(uid);
        if (session != null) {
            // 有可能在分发系统发到接入系统的过程中，刚好客户端断线了
            session.writeAndFlush(message.getBuffer());
        }
    }

    /**
     * 处理请求消息，公共逻辑，提供一个钩子，直接把消息转发给分发系统
     *
     * @param uid     用户ID
     * @param message 消息
     * @param channel 客户端连接的channel
     */
    private void handleRequestMessage(String uid, Message message, SocketChannel channel) {
        beforeDispatchMessage(uid, message, channel);
        sendMessage(uid, message);
    }

    /**
     * 钩子，转发到分发系统的逻辑，子类需要可以重写
     *
     * @param uid     用户ID
     * @param message 消息
     * @param channel 客户端连接的channel
     */
    protected void beforeDispatchMessage(String uid, Message message, SocketChannel channel) {
        // default no-op
    }


    /**
     * 发送消息到分发系统
     *
     * @param uid     用户ID
     * @param message 消息
     */
    private void sendMessage(String uid, Message message) {
        log.info("将消息发送到分发系统...uid = {},messageType = {}", uid, message.getMessageType());
        DispatcherInstance dispatcherInstance = dispatcherManager.chooseDispatcher(uid);
        dispatcherInstance.sendMessage(message.getBuffer());
    }

}
