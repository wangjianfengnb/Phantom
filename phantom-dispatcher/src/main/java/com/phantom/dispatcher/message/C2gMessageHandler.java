package com.phantom.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.common.C2gMessageRequest;
import com.phantom.common.C2gMessageResponse;
import com.phantom.common.Constants;
import com.phantom.common.Message;
import com.phantom.common.model.wrapper.C2gMessageRequestWrapper;
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
public class C2gMessageHandler extends AbstractMessageHandler<C2gMessageRequestWrapper> {

    C2gMessageHandler(SessionManager sessionManager) {
        super(sessionManager);
        Consumer consumer = new Consumer(dispatcherConfig, Constants.TOPIC_SEND_C2G_MESSAGE_RESPONSE);
        consumer.setMessageListener(message -> {
            log.info("分发系统收到发送单聊消息响应：{}", message);
            KafkaMessage msg = JSONObject.parseObject(message, KafkaMessage.class);
            //TODO 如果在刚执行execute方法就宕机，可能导致消息丢失，异步转同步消费
            execute(msg.getSenderId(), () -> {
                C2gMessageResponse response = C2gMessageResponse.newBuilder()
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
    protected C2gMessageRequestWrapper parseMessage(Message message) throws InvalidProtocolBufferException {
        C2gMessageRequest c2gMessageRequest = C2gMessageRequest.parseFrom(message.getBody());
        return C2gMessageRequestWrapper.create(c2gMessageRequest);
    }

    @Override
    protected void processMessage(C2gMessageRequestWrapper message, SocketChannel channel) {
        C2gMessageRequest c2gMessageRequest = message.getC2gMessageRequest();
        execute(c2gMessageRequest.getGroupId(), () -> {
            KafkaMessage msg = KafkaMessage.builder()
                    .senderId(c2gMessageRequest.getSenderId())
                    .groupId(c2gMessageRequest.getGroupId())
                    .content(c2gMessageRequest.getContent())
                    .timestamp(System.currentTimeMillis())
                    .crc(c2gMessageRequest.getCrc())
                    .platform(c2gMessageRequest.getPlatform())
                    .build();
            String value = JSONObject.toJSONString(msg);
            log.info("投递群聊消息到Kafka -> {}", value);
            producer.send(Constants.TOPIC_SEND_C2G_MESSAGE, c2gMessageRequest.getGroupId(), value);
        });
    }

}
