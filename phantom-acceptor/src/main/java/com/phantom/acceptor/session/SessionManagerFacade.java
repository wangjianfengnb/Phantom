package com.phantom.acceptor.session;

import com.phantom.acceptor.config.AcceptorConfig;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端会话门面
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:38
 */
@Slf4j
public class SessionManagerFacade {

    /**
     * 配置
     */
    private AcceptorConfig config;
    /**
     * 用户会话，表示接入系统内存中保存和用户的连接
     */
    private Map<String, SocketChannel> uid2Channel = new ConcurrentHashMap<>();

    /**
     * channel对应用户ID
     */
    private Map<SocketChannel, String> channel2Uid = new ConcurrentHashMap<>();

    /**
     * 代理的RedisSessionManager
     */
    private SessionManager delegate;

    public SessionManagerFacade(AcceptorConfig config) {
        this.config = config;
        this.delegate = new RedisSessionManager(config);
    }

    /**
     * 移除用户连接，只有用户发起了认证消息才会保存
     *
     * @param channel 用户连接
     * @return 是否移除了session
     */
    public boolean removeChannel(SocketChannel channel) {
        String uid = channel2Uid.remove(channel);
        if (uid != null) {
            uid2Channel.remove(uid);
            delegate.removeSession(uid);
        }
        return uid != null;
    }

    /**
     * 添加一个回话
     *
     * @param uid     用户ID
     * @param channel 渠道
     */
    public void addChannel(String uid, SocketChannel channel) {
        uid2Channel.put(uid, channel);
        channel2Uid.put(channel, uid);
    }

    public AcceptorConfig getAcceptorConfig() {
        return config;
    }

    /**
     * 获取一个回话
     *
     * @param uid 用户ID
     * @return 回话
     */
    public SocketChannel getChannel(String uid) {
        return uid2Channel.get(uid);
    }

}
