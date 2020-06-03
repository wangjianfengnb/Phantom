package com.phantom.acceptor.sso;

/**
 * 默认的认证处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 15:30
 */
public class DefaultAuthenticator implements Authenticator {
    @Override
    public boolean authenticate(String uid, String token) {
        return true;
    }
}
