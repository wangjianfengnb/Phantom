package com.phantom.client;

import com.phantom.common.AuthenticateRequest;
import com.phantom.common.C2cMessageRequest;
import com.phantom.common.C2gMessageRequest;
import com.phantom.common.Message;
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
        ConnectionManager.getInstance().initialize();
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
        connectManager.authenticate(message);
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
        C2cMessageRequest request = C2cMessageRequest.newBuilder()
                .setContent(content)
                .setSenderId(senderId)
                .setReceiverId(receiverId)
                .build();
        Message message = Message.buildC2CMessageRequest(request);
        connectManager.sendMessage(message);
    }

    /**
     * 发送一条群聊消息
     *
     * @param senderId 发送者ID
     * @param groupId  群ID
     * @param content  内容
     */
    public void sendGroupMessage(String senderId, String groupId, String content) {
        ConnectionManager connectManager = ConnectionManager.getInstance();
        C2gMessageRequest request = C2gMessageRequest.newBuilder()
                .setContent(content)
                .setSenderId(senderId)
                .setGroupId(groupId)
                .build();
        Message message = Message.buildC2gMessageRequest(request);
        connectManager.sendMessage(message);
    }

    /**
     * 添加消息监听器
     *
     * @param listener 消息监听器
     */
    public void addMessageListener(MessageListener listener) {
        ConnectionManager.getInstance().addMessageListener(listener);
    }
}
