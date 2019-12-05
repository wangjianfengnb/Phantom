package com.phantom.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.common.C2GMessageRequest;
import com.phantom.common.C2GMessageResponse;
import com.phantom.common.Constants;
import com.phantom.common.Message;
import com.phantom.dispatcher.message.wrapper.C2gMessageRequestWrapper;
import com.phantom.dispatcher.session.SessionManager;
import com.phantom.common.model.KafkaMessage;
import com.phantom.dispatcher.kafka.Consumer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 群聊消息处理
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 17:09
 */
@Slf4j
public class C2gMessageHandler extends SessionRequiredMessageHandler<C2gMessageRequestWrapper> {

    C2gMessageHandler(SessionManager sessionManager) {
        super(sessionManager);
        Consumer consumer = new Consumer(dispatcherConfig, Constants.TOPIC_SEND_C2G_MESSAGE_RESPONSE);
        consumer.setMessageListener(message -> {
            log.info("分发系统收到发送单聊消息响应：{}", message);
            KafkaMessage msg = JSONObject.parseObject(message, KafkaMessage.class);
            execute(msg.getSenderId(), () -> {
                C2GMessageResponse response = C2GMessageResponse.newBuilder()
                        .setSenderId(msg.getSenderId())
                        .setGroupId(msg.getGroupId())
                        .setTimestamp(msg.getTimestamp())
                        .setStatus(Constants.RESPONSE_STATUS_OK)
                        .setCrc(msg.getCrc())
                        .setPlatform(msg.getPlatform())
                        .setMessageId(msg.getMessageId())
                        .build();
                Message resp = Message.buildC2gMessageResponse(response);
                sendToAcceptor(msg.getSenderId(), resp);
            });
        });
    }

    @Override
    protected void processMessage(C2gMessageRequestWrapper message, SocketChannel channel) {
        C2GMessageRequest c2GMessageRequest = message.getC2GMessageRequest();
        execute(c2GMessageRequest.getGroupId(), () -> {
            KafkaMessage msg = KafkaMessage.builder()
                    .senderId(c2GMessageRequest.getSenderId())
                    .groupId(c2GMessageRequest.getGroupId())
                    .content(c2GMessageRequest.getContent())
                    .timestamp(System.currentTimeMillis())
                    .crc(c2GMessageRequest.getCrc())
                    .platform(c2GMessageRequest.getPlatform())
                    .build();
            String value = JSONObject.toJSONString(msg);
            log.info("投递群聊消息到Kafka -> {}", value);
            producer.send(Constants.TOPIC_SEND_C2G_MESSAGE, c2GMessageRequest.getSenderId(), value);
        });
    }

    @Override
    protected Message getErrorMessage(C2gMessageRequestWrapper message, SocketChannel channel) {
        C2GMessageRequest c2GMessageRequest = message.getC2GMessageRequest();
        C2GMessageResponse response = C2GMessageResponse.newBuilder()
                .setSenderId(c2GMessageRequest.getSenderId())
                .setGroupId(c2GMessageRequest.getGroupId())
                .setStatus(Constants.RESPONSE_STATUS_ERROR)
                .setTimestamp(System.currentTimeMillis())
                .setCrc(c2GMessageRequest.getCrc())
                .setPlatform(c2GMessageRequest.getPlatform())
                .build();
        return Message.buildC2gMessageResponse(response);
    }

    @Override
    protected C2gMessageRequestWrapper parseMessage(Message message) throws InvalidProtocolBufferException {
        C2GMessageRequest c2GMessageRequest = C2GMessageRequest.parseFrom(message.getBody());
        return C2gMessageRequestWrapper.create(c2GMessageRequest);
    }

}
