package com.phantom.acceptor.session;

import io.netty.channel.socket.SocketChannel;

/**
 * 回话管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 14:36
 */
public interface SessionManager {


    /**
     * 添加一个回话
     *
     * @param uid     用户ID
     * @param session 用户回话
     * @param channel 连接
     */
    void addSession(String uid, Session session, SocketChannel channel);

    /**
     * 移除用户连接
     *
     * @param channel 用户连接
     * @return 是否移除了session
     */
    boolean removeSession(SocketChannel channel);

    /**
     * 根据用户ID获取连接
     *
     * @param uid 用户ID
     * @return 回话
     */
    SocketChannel getChannel(String uid);

    /**
     * 根据连接获取用户ID
     *
     * @param channel 连接
     * @return 用户ID
     */
    String getUid(SocketChannel channel);

}
