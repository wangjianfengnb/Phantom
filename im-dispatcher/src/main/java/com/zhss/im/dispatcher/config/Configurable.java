package com.zhss.im.dispatcher.config;

/**
 * 可获取配置的
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 15:56
 */
public interface Configurable {

    /**
     * 获取配置
     *
     * @return 配置
     */
    DispatcherConfig getConfig();


}
