package com.zhss.im.dispatcher.message;

import com.alibaba.fastjson.JSONObject;
import com.zhss.im.common.Constants;
import com.zhss.im.common.Message;
import com.zhss.im.common.model.PushMessage;
import com.zhss.im.dispatcher.mq.Consumer;
import com.zhss.im.dispatcher.session.SessionManager;
import com.zhss.im.dispatcher.timeline.RedisBaseTimeline;
import com.zhss.im.dispatcher.timeline.Timeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 推送消息处理器
 * <p>
 * 采用timeline模型，对于接受者而言仅仅给他发送一个通知说有消息了，让他自己来拉取
 *
 * @author Jianfeng Wang
 * @since 2019/11/13 11:50
 */
@Slf4j
public class MessagePushHandler extends AbstractMessageHandler {

    private Timeline timeline;

    MessagePushHandler(SessionManager sessionManager) {
        super(sessionManager);
        this.timeline = new RedisBaseTimeline(dispatcherConfig);
        Consumer consumer = new Consumer(dispatcherConfig, Constants.TOPIC_SEND_C2C_MESSAGE_RESPONSE);
        consumer.setMessageListener(message -> {
            PushMessage msg = JSONObject.parseObject(message, PushMessage.class);
            // 这里收到消息推送的时候需要做两件事情
            // 1. 将消息放入timeline模型
            // 2. 发送通知给对应的客户端，让他过来拉取最新的消息


        });


    }

    @Override
    public void handleMessage(Message message, SocketChannel channel) throws Exception {
        // 这里处理抓取消息
    }
}
