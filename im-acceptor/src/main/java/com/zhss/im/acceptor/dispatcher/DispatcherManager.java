package com.zhss.im.acceptor.dispatcher;

import com.zhss.im.protocol.Constants;
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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分发系统管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/7 15:25
 */
@Slf4j
public class DispatcherManager {

    private static volatile DispatcherManager instance = new DispatcherManager();

    /**
     * 分发系统实例地址列表
     */
    private static List<DispatcherInstanceAddress> dispatcherInstanceAddresses =
            new ArrayList<>();

    private Map<String, DispatcherInstance> dispatcherInstanceMap = new ConcurrentHashMap<>();

    private DispatcherManager() {
    }

    static {
        dispatcherInstanceAddresses.add(new DispatcherInstanceAddress("localhost", "127.0.0.1", 8090));
    }

    public static DispatcherManager getInstance() {
        return instance;
    }


    public void initialize() {
        for (DispatcherInstanceAddress address : dispatcherInstanceAddresses) {
            connect(address);
        }
    }

    private void connect(DispatcherInstanceAddress address) {
        try {
            EventLoopGroup connectThreadGroup = new NioEventLoopGroup();
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
                            ch.pipeline().addLast(new DispatcherHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(address.getIp(), address.getPort()).sync();
            channelFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDispatcherInstance(String instanceId, DispatcherInstance instance) {
        dispatcherInstanceMap.put(instanceId, instance);
    }

    public void removeDispatcherInstance(String instanceId) {
        dispatcherInstanceMap.remove(instanceId);
    }

    public DispatcherInstance chooseDispatcher() {
        ArrayList<DispatcherInstance> dispatcherInstances = new ArrayList<>(dispatcherInstanceMap.values());
        Random random = new Random();
        int i = random.nextInt(dispatcherInstances.size());
        return dispatcherInstances.get(i);
    }
}
