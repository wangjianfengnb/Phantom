package com.phantom.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
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
public class PushMessageHandler extends AbstractMessageHandler {

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
            execute(getReceiverId(msg), () -> {
                // 1. 将消息放入timeline模型
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
        // 2. 发送通知给对应的客户端，让他过来拉取最新的消息
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
    public void handleMessage(Message message, SocketChannel channel) throws Exception {
        // 这里处理抓取消息
        FetchMessageRequest request = FetchMessageRequest.parseFrom(message.getBody());
        log.info("收到抓取离线消息请求：{} -> {}", request.getUid(), request.getTimestamp());
        execute(request.getUid(), () -> {
            FetchRequest fetchRequest = FetchRequest.builder()
                    .platform(request.getPlatform())
                    .size(request.getSize())
                    .uid(request.getUid())
                    .timestamp(request.getTimestamp())
                    .build();
            List<TimelineMessage> timelineMessages = timeline.fetchMessage(fetchRequest);
            FetchMessageResponse response;
            if (timelineMessages.isEmpty()) {
                response = FetchMessageResponse.newBuilder()
                        .setIsEmpty(true)
                        .setUid(request.getUid())
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
                            .setSequence(timelineMessage.getSequence());
                    if (groupId == null) {
                        messageIds.add(timelineMessage.getMessageId());
                    } else {
                        offlineMessageBuilder.setGroupId(groupId);
                    }
                    builder.addMessages(offlineMessageBuilder.build());
                }
                builder.setUid(request.getUid());
                response = builder.build();
                if (!messageIds.isEmpty()) {
                    DeliveryMessage deliveryMessage = DeliveryMessage.builder()
                            .messageIds(messageIds)
                            .build();
                    producer.send(Constants.TOPIC_DELIVERY_REPORT, "", JSONObject.toJSONString(deliveryMessage));
                }
            }
            log.info("抓取离线消息返回给客户端：{}", response);
            sendToAcceptor(request.getUid(), Message.buildFetcherMessageResponse(response));


        });


    }
}
