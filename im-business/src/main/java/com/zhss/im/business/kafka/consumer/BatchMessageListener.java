package com.zhss.im.business.kafka.consumer;

import java.util.List;

/**
 * 批量消息处理
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 14:49
 */
public interface BatchMessageListener extends MessageListener {

    /**
     * 处理消息
     *
     * @param message         消息
     * @param acknowledgement 提交offset,处理完消息一定要提交
     */
    void onMessage(List<String> message, Acknowledgement acknowledgement);

}
