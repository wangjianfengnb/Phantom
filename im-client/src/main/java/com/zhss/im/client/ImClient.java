package com.zhss.im.client;

import com.zhss.im.protocol.AuthenticateRequestProto;
import com.zhss.im.protocol.Constants;
import com.zhss.im.protocol.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
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
        AuthenticateRequestProto.AuthenticateRequest authenticateRequest =
                AuthenticateRequestProto.AuthenticateRequest.newBuilder()
                        .setTimestamp(System.currentTimeMillis())
                        .setToken(token)
                        .setUid(uid)
                        .build();
        Message message = Message.buildAuthenticateRequest(authenticateRequest);
        connectManager.sendMessage(message);
        log.info("发送认证请求...");
    }


}
