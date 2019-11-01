package com.zhss.im.gateway;

import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务接入类
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:38
 */
public class ClientManager {

    private static volatile ClientManager instance;

    /**
     * 用户对应通道
     */
    private Map<String, SocketChannel> uid2Channel = new ConcurrentHashMap<String, SocketChannel>();

    /**
     * 通道对应用户
     */
    private Map<SocketChannel, String> channel2Uid = new ConcurrentHashMap<>();

    private ClientManager() {
    }

    public static ClientManager getInstance() {
        if (instance == null) {
            synchronized (ClientManager.class) {
                if (instance == null) {
                    instance = new ClientManager();
                }
            }
        }
        return instance;
    }

    /**
     * 增加一个用户连接
     *
     * @param uid     用户ID
     * @param channel 通道
     */
    public void addClient(String uid, SocketChannel channel) {
        uid2Channel.put(uid, channel);
        channel2Uid.put(channel, uid);
    }

    /**
     * 移除用户连接
     *
     * @param channel 用户连接
     */
    public void remove(SocketChannel channel) {
        String uid = channel2Uid.remove(channel);
        uid2Channel.remove(uid);
    }


}
