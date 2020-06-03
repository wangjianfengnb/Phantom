package com.phantom.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.dispatcher.message.wrapper.C2cMessageRequestWrapper;
import com.phantom.dispatcher.session.SessionManager;
import com.phantom.common.C2CMessageRequest;
import com.phantom.common.C2CMessageResponse;
import com.phantom.common.Constants;
import com.phantom.common.Message;
import com.phantom.common.model.KafkaMessage;
import com.phantom.dispatcher.kafka.Consumer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理C2C消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 16:49
 */
@Slf4j
public class C2cMessageHandler extends SessionRequiredMessageHandler<C2cMessageRequestWrapper> {

    C2cMessageHandler(SessionManager sessionManager) {
        super(sessionManager);
        Consumer consumer = new Consumer(dispatcherConfig, Constants.TOPIC_SEND_C2C_MESSAGE_RESPONSE);
        consumer.setMessageListener(message -> {
            log.info("分发系统收到发送单聊消息响应：{}", message);
            KafkaMessage msg = JSONObject.parseObject(message, KafkaMessage.class);
            execute(msg.getSenderId(), () -> {
                C2CMessageResponse response = C2CMessageResponse.newBuilder()
                        .setSenderId(msg.getSenderId())
                        .setReceiverId(msg.getReceiverId())
                        .setTimestamp(msg.getTimestamp())
                        .setStatus(Constants.RESPONSE_STATUS_OK)
                        .setCrc(msg.getCrc())
                        .setPlatform(msg.getPlatform())
                        .setMessageId(msg.getMessageId())
                        .build();
                Message resp = Message.buildC2cMessageResponse(response);
                sendToAcceptor(msg.getSenderId(), resp);
            });
        });
    }

    @Override
    protected void processMessage(C2cMessageRequestWrapper message, SocketChannel channel) {
        C2CMessageRequest c2CMessageRequest = message.getC2cMessageRequest();
        execute(c2CMessageRequest.getReceiverId(), () -> {
            // 投递到kafka
            KafkaMessage msg = KafkaMessage.builder()
                    .senderId(c2CMessageRequest.getSenderId())
                    .receiverId(c2CMessageRequest.getReceiverId())
                    .content(c2CMessageRequest.getContent())
                    .timestamp(System.currentTimeMillis())
                    .crc(c2CMessageRequest.getCrc())
                    .platform(c2CMessageRequest.getPlatform())
                    .build();
            String value = JSONObject.toJSONString(msg);
            log.info("投递单聊消息到Kafka -> {}", value);
            // 按照接收者ID进行partition
            producer.send(Constants.TOPIC_SEND_C2C_MESSAGE, c2CMessageRequest.getReceiverId(), value);
        });
    }

    @Override
    protected Message getErrorMessage(C2cMessageRequestWrapper message, SocketChannel channel) {
        C2CMessageRequest c2CMessageRequest = message.getC2cMessageRequest();
        C2CMessageResponse response = C2CMessageResponse.newBuilder()
                .setSenderId(c2CMessageRequest.getSenderId())
                .setReceiverId(c2CMessageRequest.getReceiverId())
                .setStatus(Constants.RESPONSE_STATUS_ERROR)
                .setTimestamp(System.currentTimeMillis())
                .setCrc(c2CMessageRequest.getCrc())
                .setPlatform(c2CMessageRequest.getPlatform())
                .build();
        return Message.buildC2cMessageResponse(response);
    }

    @Override
    protected C2cMessageRequestWrapper parseMessage(Message message) throws InvalidProtocolBufferException {
        C2CMessageRequest c2CMessageRequest = C2CMessageRequest.parseFrom(message.getBody());
        return C2cMessageRequestWrapper.create(c2CMessageRequest);
    }
}
