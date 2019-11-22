package com.phantom.business.kafka.consumer;

/**
 * 单消息消费
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 14:44
 */
public interface MessageListener {

    /**
     * topic名称
     *
     * @return topic名称
     */
    String getTopic();
}
