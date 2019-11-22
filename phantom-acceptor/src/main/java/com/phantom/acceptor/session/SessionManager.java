package com.phantom.acceptor.session;

/**
 * 回话管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 14:36
 */
public interface SessionManager {

    /**
     * 删除回话
     *
     * @param uid 用户ID
     */
    void removeSession(String uid);

}
