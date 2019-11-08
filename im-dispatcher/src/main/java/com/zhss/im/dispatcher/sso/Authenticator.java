package com.zhss.im.dispatcher.sso;

/**
 * 单点登录认证器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 15:28
 */
public interface Authenticator {

    /**
     * 认证请求
     *
     * @param uid   用户ID
     * @param token token
     * @return 认证结果
     */
    boolean authenticate(String uid, String token);

}
