package com.zhss.im.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.zhss.im.common.C2CMessageRequest;
import com.zhss.im.common.C2CMessageResponse;
import com.zhss.im.common.Constants;
import com.zhss.im.common.Message;
import com.zhss.im.common.model.C2cMessage;
import com.zhss.im.dispatcher.mq.Consumer;
import com.zhss.im.dispatcher.mq.Producer;
import com.zhss.im.dispatcher.session.Session;
import com.zhss.im.dispatcher.session.SessionManager;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理C2C消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 16:49
 */
@Slf4j
public class C2cMessageHandler extends AbstractMessageHandler {

    private Producer producer;

    C2cMessageHandler(SessionManager sessionManager) {
        super(sessionManager);
        this.producer = Producer.getInstance(dispatcherConfig);
        Consumer consumer = new Consumer(dispatcherConfig, Constants.TOPIC_SEND_C2C_MESSAGE_RESPONSE);
        consumer.setMessageListener(message -> {
            log.info("分发系统收到发送单聊消息响应：{}", message);
            C2cMessage msg = JSONObject.parseObject(message, C2cMessage.class);
            C2CMessageResponse response = C2CMessageResponse.newBuilder()
                    .setSenderId(msg.getSenderId())
                    .setReceiverId(msg.getReceiverId())
                    .setTimestamp(msg.getTimestamp())
                    .setStatus(Constants.RESPONSE_STATUS_OK)
                    .build();
            Message resp = Message.buildC2cMessageResponse(response);
            sendToAcceptor(msg.getSenderId(), resp);
        });
    }

    @Override
    public void handleMessage(Message message, SocketChannel channel) throws Exception {
        // 对于C2C消息来说，直接发送到Kafka
        // 1. 先判断是否有session
        C2CMessageRequest c2CMessageRequest = C2CMessageRequest.parseFrom(message.getBody());
        Session session = sessionManager.getSession(c2CMessageRequest.getSenderId());
        if (session == null) {
            log.info("找不到Session，发送消息失败");
            C2CMessageResponse response = C2CMessageResponse.newBuilder()
                    .setSenderId(c2CMessageRequest.getSenderId())
                    .setReceiverId(c2CMessageRequest.getReceiverId())
                    .setStatus(Constants.RESPONSE_STATUS_ERROR)
                    .setTimestamp(System.currentTimeMillis())
                    .build();
            Message resp = Message.buildC2cMessageResponse(response);
            channel.writeAndFlush(resp.getBuffer());
            return;
        }
        execute(c2CMessageRequest.getReceiverId(), () -> {
            // 2. 基于snowflake算法生成messageId
            // 3. 投递到kafka
            C2cMessage msg = C2cMessage.builder()
                    .senderId(c2CMessageRequest.getSenderId())
                    .receiverId(c2CMessageRequest.getReceiverId())
                    .content(c2CMessageRequest.getContent())
                    .timestamp(System.currentTimeMillis())
                    .messageId(1L)
                    .build();
            String value = JSONObject.toJSONString(msg);
            log.info("投递单聊消息到Kafka -> {}", value);
            producer.send(Constants.TOPIC_SEND_C2C_MESSAGE, c2CMessageRequest.getSenderId(), value);
        });
    }
}
