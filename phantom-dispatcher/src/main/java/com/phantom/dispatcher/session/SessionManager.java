package com.phantom.dispatcher.session;


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
     */
    void addSession(String uid, Session session);

    /**
     * 删除回话
     *
     * @param uid 用户ID
     */
    void removeSession(String uid);

    /**
     * 获取回话
     *
     * @param uid 用户ID
     * @return 回话
     */
    Session getSession(String uid);

}
