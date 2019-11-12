package com.zhss.im.business.kafka.consumer;

/**
 * 单消息消费
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 14:50
 */
public interface SingleMessageListener extends MessageListener {


    /**
     * 处理消息
     *
     * @param message         消息
     * @param acknowledgement 提交offset,处理完消息一定要提交
     */
    void onMessage(String message, Acknowledgement acknowledgement);

}
