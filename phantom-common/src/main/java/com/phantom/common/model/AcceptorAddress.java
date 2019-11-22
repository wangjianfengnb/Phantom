package com.phantom.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接入系统服务器
 *
 * @author Jianfeng Wang
 * @since 2019/11/20 10:29
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AcceptorAddress {

    /**
     * IP地址和端口
     */
    private String ipAndPort;

}
