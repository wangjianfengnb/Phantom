package com.zhss.im.client;

import com.zhss.im.client.interceptor.MessageInterceptor;
import com.zhss.im.protocol.AuthenticateRequestProto;
import com.zhss.im.protocol.Message;

/**
 * Im客户端启动类
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 12:56
 */
public class ImClient {

    private volatile boolean initialized = false;

    public static ImClient client = new ImClient();

    private ImClient() {
    }

    public static ImClient getInstance() {
        return client;
    }


    public static void main(String[] args) {
        ImClient.getInstance().initialize();
        ImClient.getInstance().authenticate("test_uid_001", "test_token_001");
    }

    /**
     * 初始化
     */
    public void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;
        ConnectManager instance = ConnectManager.getInstance();
        instance.initialize();
        instance.addMessageInterceptor(new MessageInterceptor() {
            @Override
            public void beforeSend(Message message) {
                System.out.println("开始发送消息：" + message);
            }

            @Override
            public void afterSend(Message message) {
                System.out.println("发送消息成功：" + message);
            }
        });
    }

    /**
     * 发起认证请求
     *
     * @param uid   用户ID
     * @param token 用户Token
     */
    public void authenticate(String uid, String token) {
        ConnectManager connectManager = ConnectManager.getInstance();
        AuthenticateRequestProto.AuthenticateRequest authenticateRequest =
                AuthenticateRequestProto.AuthenticateRequest.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setToken(token)
                        .setUid(uid)
                        .build();
        Message message = Message.buildAuthenticateRequest(authenticateRequest);
        connectManager.sendMessage(message);
    }


}
