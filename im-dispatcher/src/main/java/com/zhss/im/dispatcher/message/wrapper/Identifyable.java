package com.zhss.im.dispatcher.message.wrapper;

/**
 * 用户Id感知接口
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 17:27
 */
public interface Identifyable {

    /**
     * 获取用户Id
     *
     * @return 用户ID
     */
    String getUid();
}
