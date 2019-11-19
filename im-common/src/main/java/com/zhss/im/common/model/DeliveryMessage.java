package com.zhss.im.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息投递通知
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 16:07
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DeliveryMessage {

    /**
     * 消息列表
     */
    private List<Long> messageIds;

}
