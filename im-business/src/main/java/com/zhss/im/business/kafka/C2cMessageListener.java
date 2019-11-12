package com.zhss.im.business.kafka;

import com.alibaba.fastjson.JSONObject;
import com.zhss.im.business.kafka.consumer.Acknowledgement;
import com.zhss.im.business.kafka.consumer.SingleMessageListener;
import com.zhss.im.business.kafka.producer.KafkaClient;
import com.zhss.im.business.mapper.C2cMessageMapper;
import com.zhss.im.common.Constants;
import com.zhss.im.common.model.C2cMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 单聊消息消费
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 16:00
 */
@Slf4j
@Component
public class C2cMessageListener implements SingleMessageListener {

    @Resource
    private C2cMessageMapper c2CMessageMapper;

    @Resource
    private KafkaClient kafkaClient;


    @Override
    public String getTopic() {
        return Constants.TOPIC_SEND_C2C_MESSAGE;
    }

    @Override
    public void onMessage(String message, Acknowledgement acknowledgement) {
        log.info("收到消息：{}", message);
        C2cMessage c2CMessage = JSONObject.parseObject(message, C2cMessage.class);
        c2CMessageMapper.saveMessage(c2CMessage);
        // send response
        String value = JSONObject.toJSONString(c2CMessage);
        kafkaClient.send(Constants.TOPIC_SEND_C2C_MESSAGE_RESPONSE, c2CMessage.getSenderId(), value);
        acknowledgement.ack();
    }
}
