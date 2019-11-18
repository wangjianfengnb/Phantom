package com.zhss.im.client;

import com.zhss.im.common.AuthenticateRequest;
import com.zhss.im.common.C2CMessageRequest;
import com.zhss.im.common.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * Im客户端启动类
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 12:56
 */
@Slf4j
public class ImClient {

    private static ImClient client = new ImClient();

    private ImClient() {
    }

    public static ImClient getInstance() {
        return client;
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
                        .setToken(token)
                        .setUid(uid)
                        .build();
        Message message = Message.buildAuthenticateRequest(authenticateRequest);
        connectManager.sendMessage(message);
        log.info("发送认证请求...");
    }

    /**
     * 发送一条消息
     *
     * @param senderId   发送者ID
     * @param receiverId 接受者ID
     * @param content    内容
     */
    public void sendMessage(String senderId, String receiverId, String content) {
        ConnectionManager connectManager = ConnectionManager.getInstance();
        C2CMessageRequest request = C2CMessageRequest.newBuilder()
                .setContent(content)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
        Message message = Message.buildC2CMessageRequest(request);
        connectManager.sendMessage(message);
        log.info("发送消息:{}", content);
    }



}
