package com.zhss.im.dispatcher.acceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 接入系统管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 18:22
 */
public class AcceptorServerManager {

    private static AcceptorServerManager instance = new AcceptorServerManager();

    public static AcceptorServerManager getInstance() {
        return instance;
    }

    private Map<String, AcceptorInstance> acceptorInstances = new ConcurrentHashMap<>();

    private AcceptorServerManager() {
    }

    public void addAcceptorInstance(String instanceId, AcceptorInstance instance) {
        acceptorInstances.put(instanceId, instance);
    }

    public void removeAcceptorInstance(String instanceId) {
        acceptorInstances.remove(instanceId);
    }
}
