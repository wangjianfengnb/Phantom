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
    private Map<String, SocketChannel> sessions = new ConcurrentHashMap<>();

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
     * 移除用户连接
     *
     * @param channel 用户连接
     */
    public void removeSession(SocketChannel channel) {
        String uid = channel2Uid.remove(channel);
        sessions.remove(uid);
        delegate.removeSession(uid);
    }

    /**
     * 添加一个回话
     *
     * @param uid     用户ID
     * @param channel 渠道
     */
    public void addSession(String uid, SocketChannel channel) {
        sessions.put(uid, channel);
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
    public SocketChannel getSession(String uid) {
        return sessions.get(uid);
    }

}
