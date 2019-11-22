package com.phantom.client;


import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.common.*;
import com.phantom.common.util.HttpUtil;
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
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
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


    private static String API = "http://localhost:8091/acceptor/suitable";


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
    private volatile long sequence = 0;

    /**
     * 本地最大的时间戳
     */
    private volatile long timestamp = 0;

    /**
     * 消息监听器
     */
    private List<MessageListener> messageListeners = new ArrayList<>();

    /**
     * 认证消息
     */
    private Message authenticateMessage;


    private ConnectionManager() {

    }

    public void initialize() {
        new ConnectThread().start();
        new SendMessageThread().start();
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
        if (channel != null) {
            log.info("发送认证请求...");
            channel.writeAndFlush(authenticateMessage.getBuffer());
        } else {
            isAuthenticate = false;
            log.info("接入系统宕机了，唤醒线程进行连接...");
            synchronized (this) {
                notifyAll();
            }
        }
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

    /**
     * 收到消息处理
     *
     * @param message 消息
     * @throws InvalidProtocolBufferException 序列化异常
     */
    public void onReceiveMessage(Message message) throws InvalidProtocolBufferException, InterruptedException {
        int requestType = message.getRequestType();
        if (Constants.REQUEST_TYPE_AUTHENTICATE == requestType) {
            byte[] body = message.getBody();
            AuthenticateResponse authenticateResponse =
                    AuthenticateResponse.parseFrom(body);
            if (authenticateResponse.getStatus() == Constants.RESPONSE_STATUS_OK) {
                ConnectionManager.getInstance().setAuthenticate(true);
                log.info("认证请求成功...开始拉取离线消息");
                fetchMessage(authenticateResponse.getUid());
            } else {
                log.info("认证请求失败，休眠后再次发送认证请求...");
                Thread.sleep(1000);
                channel.writeAndFlush(authenticateMessage.getBuffer());
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
                        // 要求sequence严格递增，但是由于测试阶段没有地方持久化sequence，先注释
                        //if (msg.getSequence() == sequence + 1) {
                        sequence++;
                        timestamp = msg.getTimestamp();
                        for (MessageListener listener : messageListeners) {
                            listener.onMessage(msg);
                        }
                        // } else {
                        //    log.info("发现获取到的消息sequence不连续，丢弃后续的消息");
                        //    break;
                        //}
                    }
                    fetchMessage(response.getUid());
                }
            }
        } else if (Constants.REQUEST_TYPE_C2G_SEND == requestType) {
            byte[] body = message.getBody();
            C2GMessageResponse c2GMessageResponse =
                    C2GMessageResponse.parseFrom(body);
            if (c2GMessageResponse.getStatus() == Constants.RESPONSE_STATUS_OK) {
                log.info("发送群聊消息成功...");
            } else {
                log.info("发送群聊消息失败，重新发送...");
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
                .setTimestamp(timestamp)
                .setUid(uid)
                .build();
        this.sendMessage(Message.buildFetcherMessageRequest(request));
    }

    /**
     * 保存认证信息
     *
     * @param message message
     */
    public void authenticate(Message message) {
        this.authenticateMessage = message;
    }

    /**
     * 用于建立连接的线程
     */
    class ConnectThread extends Thread {
        @Override
        public void run() {
            while (!shutdown) {
                try {
                    String s = HttpUtil.get(API, null);
                    JSONObject jsonObject = JSONObject.parseObject(s);
                    if (StringUtil.isNullOrEmpty(s)) {
                        Thread.sleep(5 * 1000);
                        continue;
                    }
                    String ipAndPort = jsonObject.getString("ipAndPort");
                    String ip = ipAndPort.split(":")[0];
                    int port = Integer.valueOf(ipAndPort.split(":")[1]);
                    log.info("开始和接入系统发起连接....");
                    connectThreadGroup = new NioEventLoopGroup();
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
                    synchronized (ConnectionManager.this) {
                        log.info("连接接入系统成功，休眠......");
                        ConnectionManager.this.wait();
                    }
                } catch (Exception e) {
                    try {
                        log.info("连接接入系统发生异常，休眠{} ms后开始重新连接", 5000);
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 用于发送消息的线程
     */
    class SendMessageThread extends Thread {
        @Override
        public void run() {
            while (!shutdown) {
                try {
                    Message msg = messages.poll(10, TimeUnit.SECONDS);
                    if (msg == null) {
                        continue;
                    }
                    while (!isAuthenticate) {
                        Thread.sleep(1000);
                    }
                    if (channel != null) {
                        log.info("发送消息：requestType = {}", Constants.requestTypeName(msg.getRequestType()));
                        channel.writeAndFlush(msg.getBuffer());
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
