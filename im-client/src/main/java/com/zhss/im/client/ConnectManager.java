package com.zhss.im.client;

import com.zhss.im.client.interceptor.MessageInterceptor;
import com.zhss.im.protocol.Constants;
import com.zhss.im.protocol.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * 连接管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 13:48
 */
public class ConnectManager {

    /**
     * state disconnected
     */
    public static final int STATE_DISCONNECTED = 1 << 1;
    /**
     * state connecting
     */
    public static final int STATE_CONNECTING = 1 << 2;
    /**
     * state which mean connection is established
     */
    public static final int STATE_CONNECTED = 1 << 3;
    /**
     * state which mean Client is ready to send message
     */
    public static final int STATE_AUTHENTICATE = 1 << 4;

    /**
     * IO thread-pool coreSize
     */
    public static final int coreSize = 3;

    private volatile int state = STATE_DISCONNECTED;

    private volatile boolean shutDown = false;

    private SocketChannel socketChannel;

    private BlockingQueue<Message> requestQueue = new LinkedBlockingDeque<>(1000);

    private List<MessageInterceptor> interceptors = new ArrayList<>();

    private ThreadPoolExecutor executor;

    private ConnectManager() {
    }

    public void addMessageInterceptor(MessageInterceptor messageInterceptor) {
        interceptors.add(messageInterceptor);
    }

    public void initialize() {
        new ConnectThread().start();
        this.executor = new ThreadPoolExecutor(coreSize, coreSize, 0, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setName("ConnectManage-IO-thread");
                    return t;
                });
        for (int i = 0; i < coreSize; i++) {
            executor.execute(this::runTask);
        }

    }

    private void runTask() {
        Message msg;
        while (!shutDown) {
            try {
                msg = requestQueue.take();
                while (state != STATE_AUTHENTICATE) {
                    Thread.sleep(1000);
                }
                invokeMessageSend(msg);
                socketChannel.writeAndFlush(msg.getBuffer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void invokeMessageSend(Message msg) {
        for (MessageInterceptor interceptor : interceptors) {
            interceptor.beforeSend(msg);
        }
    }

    private void invokeMessageReceive(Message msg) {
        for (MessageInterceptor interceptor : interceptors) {
            interceptor.afterSend(msg);
        }
    }

    /**
     * 建立连接，保存连接
     */
    public void onChannelActive(SocketChannel channel) {
        socketChannel = channel;
        state = STATE_CONNECTED;
    }

    /**
     * 断开连接
     */
    public void onChannelInActive(SocketChannel channel) {
        socketChannel = null;
        state = STATE_DISCONNECTED;
        synchronized (this) {
            notifyAll();
        }
    }

    public void sendMessage(Message message) {
        requestQueue.add(message);
    }

    public void onMessageReceive(Message message) throws Exception {
        invokeMessageReceive(message);
        int requestType = message.getRequestType();
        int messageType = message.getMessageType();
        if (Constants.REQUEST_TYPE_AUTHENTICATE == requestType) {
            System.out.println("收到认证请求的响应...");
        }
    }

    private static class Singleton {
        static ConnectManager instance;
        static {
            instance = new ConnectManager();
        }
    }

    public static ConnectManager getInstance() {
        return Singleton.instance;
    }


    private class ConnectThread extends Thread {

        ConnectThread() {
            setName("im_client_connect_thread");
        }

        @Override
        public void run() {
            while (!shutDown) {
                if (state == STATE_DISCONNECTED) {
                    state = STATE_CONNECTING;
                    System.out.println("Start to connection AcceptorServer.");
                    EventLoopGroup connectThreadGroup = new NioEventLoopGroup();
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
                                        ch.pipeline().addLast(new ImClientHandler());
                                    }
                                });
                        ChannelFuture channelFuture = bootstrap.connect("localhost", 8080).sync();
                        channelFuture.channel().closeFuture().sync();
                    } catch (Exception e) {
                        state = STATE_DISCONNECTED;
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    } finally {
                        connectThreadGroup.shutdownGracefully();
                    }
                } else {
                    try {
                        synchronized (ConnectManager.this) {
                            ConnectManager.this.wait();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
