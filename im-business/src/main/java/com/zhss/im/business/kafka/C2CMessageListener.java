package com.zhss.im.business.kafka;

import com.zhss.im.protocol.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 单聊消息消费
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 16:00
 */
@Slf4j
@Component
public class C2CMessageListener implements SingleMessageListener {

    @Override
    public String getTopic() {
        return Constants.TOPIC_SEND_C2C_MESSAGE;
    }

    @Override
    public void onMessage(String message, Acknowledgement acknowledgement) {
        log.info("收到消息：{}", message);
        acknowledgement.ack();
    }
}
