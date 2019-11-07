package com.zhss.im.acceptor;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务接入类
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:38
 */
@Slf4j
public class ClientManager {

    private static volatile ClientManager instance;


    /**
     * 用户对应通道
     */
    private Map<String, SocketChannel> clients = new ConcurrentHashMap<>();

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
     * 移除用户连接
     *
     * @param channel 用户连接
     */
    public void removeClient(SocketChannel channel) {
        String uid = channel2Uid.remove(channel);
        clients.remove(uid);
    }

    /**
     * 添加未认证的连接
     *
     * @param uid     用户ID
     * @param channel 渠道
     */
    public void addClient(String uid, SocketChannel channel) {
        clients.put(uid, channel);
        channel2Uid.put(channel, uid);
    }

    public SocketChannel getClient(String uid) {
        return clients.get(uid);
    }

}
