package com.zhss.im.dispatcher.timeline;

import lombok.Builder;
import lombok.Data;

/**
 * 抓取数据请求
 *
 * @author Jianfeng Wang
 * @since 2019/11/18 13:49
 */
@Data
@Builder
public class FetchRequest {

    /**
     * 需要抓取消息的uid
     */
    private String uid;

    /**
     * 需要抓取消息条数
     */

    private int size;
    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 消息平台 1-APP 2-Web
     */
    private int platform;
}
