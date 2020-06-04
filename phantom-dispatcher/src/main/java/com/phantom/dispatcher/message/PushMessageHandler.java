package com.phantom.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.common.*;
import com.phantom.dispatcher.session.SessionManager;
import com.phantom.dispatcher.timeline.FetchRequest;
import com.phantom.dispatcher.timeline.RedisBaseTimeline;
import com.phantom.dispatcher.timeline.Timeline;
import com.phantom.dispatcher.timeline.TimelineMessage;
import com.phantom.common.model.DeliveryMessage;
import com.phantom.common.model.KafkaMessage;
import com.phantom.dispatcher.kafka.Consumer;
import com.phantom.dispatcher.kafka.Producer;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 推送消息处理器
 * <p>
 * 采用timeline模型，对于接受者而言仅仅给他发送一个通知说有消息了，让他自己来拉取
 *
 * @author Jianfeng Wang
 * @since 2019/11/13 11:50
 */
@Slf4j
public class PushMessageHandler extends AbstractMessageHandler<FetchMessageRequest> {

    private Timeline timeline;

    private Producer producer;

    PushMessageHandler(SessionManager sessionManager) {
        super(sessionManager);
        this.timeline = new RedisBaseTimeline(dispatcherConfig);
        this.producer = Producer.getInstance(dispatcherConfig);
        Consumer consumer = new Consumer(dispatcherConfig,
                Constants.TOPIC_SEND_C2C_MESSAGE_RESPONSE,
                Constants.TOPIC_PUSH_MESSAGE);
        consumer.setMessageListener(message -> {
            KafkaMessage msg = JSONObject.parseObject(message, KafkaMessage.class);
            //TODO 如果在刚执行execute方法就宕机，可能导致消息丢失，异步转同步消费
            execute(getReceiverId(msg), () -> {
                // 将消息放入timeline模型
                log.info("分发系统收到消息推送请求：{}", msg);
                if (msg.getGroupId() == null) {
                    TimelineMessage timelineMessage = TimelineMessage.parseC2CMessage(msg);
                    timeline.saveMessage(timelineMessage);
                    sendInformMessage(timelineMessage);
                } else {
                    List<TimelineMessage> timelineMessages = TimelineMessage.parseC2GMessage(msg);
                    for (TimelineMessage timelineMessage : timelineMessages) {
                        timeline.saveMessage(timelineMessage);
                        sendInformMessage(timelineMessage);
                    }
                }
            });
        });
    }

    /**
     * 发送通知消息给客户端
     *
     * @param timelineMessage 通知消息
     */
    private void sendInformMessage(TimelineMessage timelineMessage) {
        log.info("下发通知给客户端，让客户端过来拉取消息...uid = {}", timelineMessage.getReceiverId());
        // 发送通知给对应的客户端，让他过来拉取最新的消息
        InformFetchMessageResponse informFetchMessageResponse = InformFetchMessageResponse.newBuilder()
                .setUid(timelineMessage.getReceiverId())
                .build();
        Message response = Message.buildInformFetchMessageResponse(informFetchMessageResponse);
        sendToAcceptor(timelineMessage.getReceiverId(), response);
    }

    private String getReceiverId(KafkaMessage message) {
        return message.getGroupId() == null ? message.getReceiverId() : message.getGroupId();
    }

    @Override
    protected FetchMessageRequest parseMessage(Message message) throws InvalidProtocolBufferException {
        return FetchMessageRequest.parseFrom(message.getBody());
    }

    @Override
    protected void processMessage(FetchMessageRequest message, SocketChannel channel) {
        log.info("收到抓取离线消息请求：{} -> {}", message.getUid(), message.getTimestamp());
        execute(message.getUid(), () -> {
            FetchRequest fetchRequest = FetchRequest.builder()
                    .platform(message.getPlatform())
                    .size(message.getSize())
                    .uid(message.getUid())
                    .timestamp(message.getTimestamp())
                    .build();
            List<TimelineMessage> timelineMessages = timeline.fetchMessage(fetchRequest);
            FetchMessageResponse response;
            if (timelineMessages.isEmpty()) {
                response = FetchMessageResponse.newBuilder()
                        .setIsEmpty(true)
                        .setUid(message.getUid())
                        .build();
            } else {
                FetchMessageResponse.Builder builder = FetchMessageResponse.newBuilder()
                        .setIsEmpty(false);
                List<Long> messageIds = new ArrayList<>(timelineMessages.size());
                for (TimelineMessage timelineMessage : timelineMessages) {
                    String groupId = timelineMessage.getGroupId();
                    OfflineMessage.Builder offlineMessageBuilder = OfflineMessage.newBuilder()
                            .setSenderId(timelineMessage.getSenderId())
                            .setReceiverId(timelineMessage.getReceiverId())
                            .setContent(timelineMessage.getContent())
                            .setMessageId(timelineMessage.getMessageId())
                            .setTimestamp(timelineMessage.getTimestamp())
                            .setSequence(timelineMessage.getSequence())
                            .setCrc(timelineMessage.getCrc())
                            .setPlatform(timelineMessage.getPlatform());
                    if (groupId == null) {
                        messageIds.add(timelineMessage.getMessageId());
                    } else {
                        offlineMessageBuilder.setGroupId(groupId);
                    }
                    builder.addMessages(offlineMessageBuilder.build());
                }
                builder.setUid(message.getUid());
                response = builder.build();
                if (!messageIds.isEmpty()) {
                    DeliveryMessage deliveryMessage = DeliveryMessage.builder()
                            .messageIds(messageIds)
                            .build();
                    producer.send(Constants.TOPIC_DELIVERY_REPORT, "", JSONObject.toJSONString(deliveryMessage));
                }
            }
            log.info("抓取离线消息返回给客户端：{}", response);
            sendToAcceptor(message.getUid(), Message.buildFetcherMessageResponse(response));
        });
    }
}
