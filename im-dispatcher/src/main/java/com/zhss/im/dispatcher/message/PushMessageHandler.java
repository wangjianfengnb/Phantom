package com.zhss.im.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.zhss.im.common.*;
import com.zhss.im.common.model.DeliveryMessage;
import com.zhss.im.common.model.PushMessage;
import com.zhss.im.dispatcher.mq.Consumer;
import com.zhss.im.dispatcher.mq.Producer;
import com.zhss.im.dispatcher.session.SessionManager;
import com.zhss.im.dispatcher.timeline.FetchRequest;
import com.zhss.im.dispatcher.timeline.RedisBaseTimeline;
import com.zhss.im.dispatcher.timeline.Timeline;
import com.zhss.im.dispatcher.timeline.TimelineMessage;
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
            PushMessage msg = JSONObject.parseObject(message, PushMessage.class);
            // 这里收到消息推送的时候需要做两件事情
            execute(msg.getReceiverId(), () -> {
                // 1. 将消息放入timeline模型
                log.info("分发系统收到消息推送请求：{}", msg);
                if (msg.getGroupId() == null) {
                    TimelineMessage timelineMessage = TimelineMessage.parseC2CMessage(msg);
                    timeline.saveMessage(timelineMessage);
                } else {
                    List<TimelineMessage> timelineMessages = TimelineMessage.parseC2GMessage(msg);
                    for (TimelineMessage timelineMessage : timelineMessages) {
                        timeline.saveMessage(timelineMessage);
                    }
                }
                log.info("下发通知给客户端，让客户端过来拉取消息...");
                // 2. 发送通知给对应的客户端，让他过来拉取最新的消息
                InformFetchMessageResponse informFetchMessageResponse = InformFetchMessageResponse.newBuilder()
                        .setUid(msg.getReceiverId())
                        .build();
                Message response = Message.buildInformFetchMessageResponse(informFetchMessageResponse);
                sendToAcceptor(msg.getReceiverId(), response);
            });
        });
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
                    long groupId = timelineMessage.getGroupId() == null ? -1 : timelineMessage.getGroupId();
                    builder.addMessages(OfflineMessage.newBuilder()
                            .setSenderId(timelineMessage.getSenderId())
                            .setReceiverId(timelineMessage.getReceiverId())
                            .setContent(timelineMessage.getContent())
                            .setGroupId(groupId)
                            .setMessageId(timelineMessage.getMessageId())
                            .setTimestamp(timelineMessage.getTimestamp())
                            .setSequence(timelineMessage.getSequence())
                            .build());
                    if (timelineMessage.getGroupId() == null) {
                        messageIds.add(timelineMessage.getMessageId());
                    }
                }
                builder.setUid(request.getUid());
                response = builder.build();
                DeliveryMessage deliveryMessage = DeliveryMessage.builder()
                        .messageIds(messageIds)
                        .build();
                producer.send(Constants.TOPIC_DELIVERY_REPORT, "", JSONObject.toJSONString(deliveryMessage));


            }
            log.info("抓取离线消息返回给客户端：{}", response);
            sendToAcceptor(request.getUid(), Message.buildFetcherMessageResponse(response));


        });


    }
}
