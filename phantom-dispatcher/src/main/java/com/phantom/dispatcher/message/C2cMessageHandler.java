package com.phantom.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.common.C2cMessageRequest;
import com.phantom.common.C2cMessageResponse;
import com.phantom.common.model.wrapper.C2cMessageRequestWrapper;
import com.phantom.dispatcher.session.SessionManager;
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
public class C2cMessageHandler extends AbstractMessageHandler<C2cMessageRequestWrapper> {

    C2cMessageHandler(SessionManager sessionManager) {
        super(sessionManager);
        Consumer consumer = new Consumer(dispatcherConfig, Constants.TOPIC_SEND_C2C_MESSAGE_RESPONSE);
        consumer.setMessageListener(message -> {
            log.info("分发系统收到发送单聊消息响应：{}", message);
            KafkaMessage msg = JSONObject.parseObject(message, KafkaMessage.class);
            //TODO 如果在刚执行execute方法就宕机，可能导致消息丢失，异步转同步消费
            execute(msg.getSenderId(), () -> {
                C2cMessageResponse response = C2cMessageResponse.newBuilder()
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
    protected C2cMessageRequestWrapper parseMessage(Message message) throws InvalidProtocolBufferException {
        C2cMessageRequest c2cMessageRequest = C2cMessageRequest.parseFrom(message.getBody());
        return C2cMessageRequestWrapper.create(c2cMessageRequest);
    }

    @Override
    protected void processMessage(C2cMessageRequestWrapper message, SocketChannel channel) {
        C2cMessageRequest c2cMessageRequest = message.getC2cMessageRequest();
        execute(c2cMessageRequest.getReceiverId(), () -> {
            // 投递到kafka
            KafkaMessage msg = KafkaMessage.builder()
                    .senderId(c2cMessageRequest.getSenderId())
                    .receiverId(c2cMessageRequest.getReceiverId())
                    .content(c2cMessageRequest.getContent())
                    .timestamp(System.currentTimeMillis())
                    .crc(c2cMessageRequest.getCrc())
                    .platform(c2cMessageRequest.getPlatform())
                    .build();
            String value = JSONObject.toJSONString(msg);
            log.info("投递单聊消息到Kafka -> {}", value);
            // 按照接收者ID进行partition
            producer.send(Constants.TOPIC_SEND_C2C_MESSAGE, c2cMessageRequest.getReceiverId(), value);
        });
    }

}
