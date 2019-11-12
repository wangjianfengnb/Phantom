package com.zhss.im.business.kafka;

/**
 * 消息确认ack
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 16:07
 */
public interface Acknowledgement {

    /**
     * 提交offset
     */
    void ack();

}
