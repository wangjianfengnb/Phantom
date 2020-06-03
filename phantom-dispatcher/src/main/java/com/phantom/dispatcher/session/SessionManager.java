package com.phantom.dispatcher.session;


/**
 * 回话管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 14:36
 */
public interface SessionManager {

    /**
     * 获取回话
     *
     * @param uid 用户ID
     * @return 回话
     */
    Session getSession(String uid);

}
