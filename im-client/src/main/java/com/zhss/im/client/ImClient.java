package com.zhss.im.client;

import com.zhss.im.protocol.AuthenticateRequest;
import com.zhss.im.protocol.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * Im客户端启动类
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 12:56
 */
@Slf4j
public class ImClient {

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
        ConnectionManager.getInstance().connect("localhost", 8080);
    }

    /**
     * 发起认证请求
     *
     * @param uid   用户ID
     * @param token 用户Token
     */
    public void authenticate(String uid, String token) {
        ConnectionManager connectManager = ConnectionManager.getInstance();
        AuthenticateRequest authenticateRequest =
                AuthenticateRequest.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setToken(token)
                        .setUid(uid)
                        .build();
        Message message = Message.buildAuthenticateRequest(authenticateRequest);
        connectManager.sendMessage(message);
        log.info("发送认证请求...");
    }


}
