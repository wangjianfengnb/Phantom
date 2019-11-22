package com.phantom.dispatcher.acceptor;

import com.phantom.common.util.NetUtils;
import io.netty.channel.socket.SocketChannel;

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

    /**
     * 用户管理接入系统的，acceptorInstanceId -> acceptor
     */
    private Map<String, AcceptorInstance> acceptorInstances = new ConcurrentHashMap<>();

    /**
     * 渠道ID对应接入系统ID
     */
    private Map<String, String> channelId2AcceptorId = new ConcurrentHashMap<>();

    private AcceptorServerManager() {
    }

    /**
     * 添加接入系统
     *
     * @param acceptorInstanceId 接入系统ID
     * @param instance           接入系统实例
     */
    public void addAcceptorInstance(String acceptorInstanceId, AcceptorInstance instance) {
        acceptorInstances.put(acceptorInstanceId, instance);
        channelId2AcceptorId.put(NetUtils.getChannelId(instance.getChannel()), acceptorInstanceId);
    }

    /**
     * 删除连接
     *
     * @param channelId 连接
     */
    public void removeAcceptorInstance(String channelId) {
        String acceptorInstanceId = channelId2AcceptorId.remove(channelId);
        acceptorInstances.remove(acceptorInstanceId);
    }

    /**
     * 根据接入系统ID获取接入系统实例
     *
     * @param acceptorInstanceId 实例ID
     * @return 实例
     */
    public AcceptorInstance getAcceptorInstance(String acceptorInstanceId) {
        return acceptorInstances.get(acceptorInstanceId);
    }

    /**
     * 获取接入系统的实例ID
     *
     * @param channelId 通道ID
     * @return 实例ID
     */
    public String getAcceptorInstanceId(String channelId) {
        return channelId2AcceptorId.get(channelId);
    }

}
