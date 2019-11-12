package com.zhss.im.acceptor.dispatcher;

import com.zhss.im.acceptor.config.AcceptorConfig;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.common.Constants;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * 分发系统管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/7 15:25
 */
@Slf4j
public class DispatcherManager {


    /**
     * 分发系统实例地址列表
     */
    private static List<DispatcherInstanceAddress> dispatcherInstanceAddresses =
            new ArrayList<>();

    static {
        // 后面基于zk来做
        dispatcherInstanceAddresses.add(new DispatcherInstanceAddress("localhost", "127.0.0.1", 8090));
    }

    private final SessionManagerFacade sessionManagerFacade;


    /**
     * 配置
     */
    private AcceptorConfig config;

    /**
     * 分发服务器实例
     */
    private Map<String, DispatcherInstance> dispatcherInstanceMap = new ConcurrentHashMap<>();

    public DispatcherManager(AcceptorConfig config, SessionManagerFacade sessionManagerFacade) {
        this.config = config;
        this.sessionManagerFacade = sessionManagerFacade;
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
                            ch.pipeline().addLast(new DelimiterBasedFrameDecoder(config.getMaxMessageBytes(),
                                    Unpooled.copiedBuffer(Constants.DELIMITER)));
                            ch.pipeline().addLast(new DispatcherHandler(DispatcherManager.this, sessionManagerFacade));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(address.getIp(), address.getPort()).sync();
            channelFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addDispatcherInstance(String instanceId, DispatcherInstance instance) {
        this.dispatcherInstanceMap.put(instanceId, instance);
    }

    public void removeDispatcherInstance(String instanceId) {
        dispatcherInstanceMap.remove(instanceId);
    }

    public DispatcherInstance chooseDispatcher(String uid) {
        ArrayList<DispatcherInstance> dispatcherInstances = new ArrayList<>(dispatcherInstanceMap.values());
        int hash = toPositive(murmur2(uid.getBytes())) % dispatcherInstances.size();
        int index = hash % dispatcherInstances.size();
        return dispatcherInstances.get(index);
    }


    public static int toPositive(int number) {
        return number & 0x7fffffff;
    }

    /**
     * Generates 32 bit murmur2 hash from byte array
     *
     * @param data byte array to hash
     * @return 32 bit hash of the given array
     */
    public static int murmur2(final byte[] data) {
        int length = data.length;
        int seed = 0x9747b28c;
        // 'm' and 'r' are mixing constants generated offline.
        // They're not really 'magic', they just happen to work well.
        final int m = 0x5bd1e995;
        final int r = 24;

        // Initialize the hash to a random value
        int h = seed ^ length;
        int length4 = length / 4;

        for (int i = 0; i < length4; i++) {
            final int i4 = i * 4;
            int k = (data[i4 + 0] & 0xff) + ((data[i4 + 1] & 0xff) << 8) + ((data[i4 + 2] & 0xff) << 16) + ((data[i4 + 3] & 0xff) << 24);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // Handle the last few bytes of the input array
        switch (length % 4) {
            case 3:
                h ^= (data[(length & ~3) + 2] & 0xff) << 16;
            case 2:
                h ^= (data[(length & ~3) + 1] & 0xff) << 8;
            case 1:
                h ^= data[length & ~3] & 0xff;
                h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;
        return h;
    }

}
