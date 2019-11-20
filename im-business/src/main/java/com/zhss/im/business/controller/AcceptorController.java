package com.zhss.im.business.controller;

import com.zhss.im.business.zookeeper.AcceptorListManager;
import com.zhss.im.common.model.AcceptorAddress;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 负责分发服务的IPlist
 *
 * @author Jianfeng Wang
 * @since 2019/11/20 10:27
 */
@RestController
@RequestMapping("/acceptor")
public class AcceptorController {

    @Resource
    private AcceptorListManager acceptorListManager;

    /**
     * 获取合适的Acceptor地址
     */
    @GetMapping("/suitable")
    public AcceptorAddress getOne() {
        String ipAddress = acceptorListManager.getFirst();
        return AcceptorAddress.builder()
                .ipAndPort(ipAddress)
                .build();
    }
}
