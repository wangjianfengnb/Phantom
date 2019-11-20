package com.zhss.im.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.zhss.im.common.C2CMessageRequest;
import com.zhss.im.common.C2CMessageResponse;
import com.zhss.im.common.Constants;
import com.zhss.im.common.Message;
import com.zhss.im.common.model.KafkaMessage;
import com.zhss.im.dispatcher.message.wrapper.C2cMessageRequestWrapper;
import com.zhss.im.dispatcher.kafka.Consumer;
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
            // 2. 基于snowflake算法生成messageId
            // 3. 投递到kafka
            KafkaMessage msg = KafkaMessage.builder()
                    .senderId(c2CMessageRequest.getSenderId())
                    .receiverId(c2CMessageRequest.getReceiverId())
                    .content(c2CMessageRequest.getContent())
                    .timestamp(System.currentTimeMillis())
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
                .build();
        return Message.buildC2cMessageResponse(response);
    }

    @Override
    protected C2cMessageRequestWrapper parseMessage(Message message) throws InvalidProtocolBufferException {
        C2CMessageRequest c2CMessageRequest = C2CMessageRequest.parseFrom(message.getBody());
        return C2cMessageRequestWrapper.create(c2CMessageRequest);
    }
}
