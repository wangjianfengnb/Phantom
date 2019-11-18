package com.zhss.im.client;


import com.google.protobuf.InvalidProtocolBufferException;
import com.zhss.im.common.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 连接管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/7 18:29
 */
@Slf4j
public class ConnectionManager {

    /**
     * 单例
     */
    private static ConnectionManager connectionManager = new ConnectionManager();

    /**
     * 和服务端的通道
     */
    private volatile SocketChannel channel;

    /**
     * 消息发送队列
     */
    private ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<>(1000);

    /**
     * 线程池
     */
    private ThreadPoolExecutor threadPool = null;

    /**
     * 是否shutdown
     */
    private volatile boolean shutdown = false;
    /**
     * 事件循环处理组
     */
    private EventLoopGroup connectThreadGroup = null;

    /**
     * 当前是否已经认证通过
     */
    private volatile boolean isAuthenticate = false;

    /**
     * 本地最大的sequence
     */
    private volatile long maxSequence = 0;

    /**
     * 本地最大的时间戳
     */
    private volatile long maxTimestamp = 0;

    /**
     * 消息监听器
     */
    private List<MessageListener> messageListeners = new ArrayList<>();


    private ConnectionManager() {
    }

    public void connect(String ip, int port) {
        log.info("开始和接入系统发起连接....");
        connectThreadGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(connectThreadGroup)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10 * 1000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(4096,
                                    Unpooled.copiedBuffer(Constants.DELIMITER)));
                            ch.pipeline().addLast(new ImClientHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
            channelFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
        threadPool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(), r -> new Thread(r, "Connect-IO-Thread"));
        threadPool.execute(() -> {
            while (!shutdown) {
                try {
                    Message msg = messages.poll(10, TimeUnit.SECONDS);
                    if (msg == null) {
                        continue;
                    }
                    if (msg.getRequestType() == Constants.REQUEST_TYPE_AUTHENTICATE) {
                        while (channel == null) {
                            Thread.sleep(1000);
                        }
                    } else {
                        while (!isAuthenticate) {
                            Thread.sleep(1000);
                        }
                    }
                    if (channel != null) {
                        log.info("发送消息：{}", msg.getRequestType());
                        channel.writeAndFlush(msg.getBuffer());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    public void setAuthenticate(boolean authenticate) {
        this.isAuthenticate = authenticate;
    }


    public static ConnectionManager getInstance() {
        return connectionManager;
    }


    public void setChannel(SocketChannel channel) {
        this.channel = channel;
    }

    public void sendMessage(Message message) {
        messages.add(message);
    }

    public void shutdown() {
        this.shutdown = true;
        if (threadPool != null) {
            this.threadPool.shutdown();
            this.threadPool = null;
        }
        if (this.connectThreadGroup != null) {
            this.connectThreadGroup.shutdownGracefully();
            this.connectThreadGroup = null;
        }
    }

    public void onReceiveMessage(Message message) throws InvalidProtocolBufferException {
        int requestType = message.getRequestType();
        if (Constants.REQUEST_TYPE_AUTHENTICATE == requestType) {
            byte[] body = message.getBody();
            AuthenticateResponse authenticateResponse =
                    AuthenticateResponse.parseFrom(body);
            if (authenticateResponse.getStatus() == Constants.RESPONSE_STATUS_OK) {
                log.info("认证请求成功...");
                ConnectionManager.getInstance().setAuthenticate(true);
            } else {
                log.info("认证请求失败...");
            }
        } else if (Constants.REQUEST_TYPE_C2C_SEND == requestType) {
            byte[] body = message.getBody();
            C2CMessageResponse c2CMessageResponse =
                    C2CMessageResponse.parseFrom(body);
            if (c2CMessageResponse.getStatus() == Constants.RESPONSE_STATUS_OK) {
                log.info("发送单聊消息成功...");
            } else {
                log.info("发送单聊消息失败，重新发送...");
            }
        } else if (Constants.REQUEST_TYPE_INFORM_FETCH == requestType) {
            log.info("收到抓取离线消息的通知...");
            InformFetchMessageResponse informFetchMessageResponse =
                    InformFetchMessageResponse.parseFrom(message.getBody());
            String uid = informFetchMessageResponse.getUid();
            fetchMessage(uid);
        } else if (Constants.REQUEST_TYPE_MESSAGE_FETCH == requestType) {
            FetchMessageResponse response = FetchMessageResponse.parseFrom(message.getBody());
            boolean isEmpty = response.getIsEmpty();
            log.info("收到抓取离线消息的响应，是否结果为空：{}", isEmpty);
            if (!isEmpty) {
                synchronized (ConnectionManager.class) {
                    List<OfflineMessage> messagesList = response.getMessagesList();
                    for (OfflineMessage msg : messagesList) {
                        if (msg.getSequence() == maxSequence + 1) {
                            maxSequence++;
                            maxTimestamp = msg.getTimestamp();
                            // TODO sequence需要落地磁盘，一般就是数据库mysql
                            for (MessageListener listener : messageListeners) {
                                listener.onMessage(msg);
                            }
                        } else {
                            log.info("发现获取到的消息sequence不连续，丢弃后续的消息");
                            break;
                        }
                    }
                    fetchMessage(response.getUid());
                }
            }
        }
    }

    /**
     * 抓取消息
     *
     * @param uid 用户ID
     */
    private void fetchMessage(String uid) {
        FetchMessageRequest request = FetchMessageRequest.newBuilder()
                .setPlatform(1)
                .setSize(10)
                .setTimestamp(maxTimestamp)
                .setUid(uid)
                .build();
        log.info("发送抓取离线消息：{}", request);
        this.sendMessage(Message.buildFetcherMessageRequest(request));
    }
}
