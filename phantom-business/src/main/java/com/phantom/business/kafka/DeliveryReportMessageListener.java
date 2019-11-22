package com.phantom.business.kafka;

import com.alibaba.fastjson.JSONObject;
import com.phantom.business.kafka.consumer.SingleMessageListener;
import com.phantom.business.mapper.C2cMessageMapper;
import com.phantom.business.kafka.consumer.Acknowledgement;
import com.phantom.common.Constants;
import com.phantom.common.model.DeliveryMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Jianfeng Wang
 * @since 2019/11/19 16:11
 */
@Slf4j
@Component
public class DeliveryReportMessageListener implements SingleMessageListener {

    @Resource
    private C2cMessageMapper c2CMessageMapper;

    @Override
    public void onMessage(String message, Acknowledgement acknowledgement) {
        log.info("收到消息投递状态报告：" + message);
        DeliveryMessage deliveryMessage = JSONObject.parseObject(message, DeliveryMessage.class);
        List<Long> messageIds = deliveryMessage.getMessageIds();
        for (Long messageId : messageIds) {
            c2CMessageMapper.updateMessageDeliverySuccess(messageId);
        }
        acknowledgement.ack();
    }

    @Override
    public String getTopic() {
        return Constants.TOPIC_DELIVERY_REPORT;
    }
}
