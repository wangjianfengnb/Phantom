package com.phantom.acceptor.dispatcher;

import com.phantom.acceptor.config.AcceptorConfig;
import com.phantom.acceptor.session.SessionManagerFacade;
import com.phantom.common.Constants;
import com.phantom.common.util.StringUtils;
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
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

import java.util.*;
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
     * Session管理
     */
    private SessionManagerFacade sessionManagerFacade;

    /**
     * 代表当前系统的唯一ID
     */
    private String acceptorInstanceId;

    /**
     * 配置
     */
    private AcceptorConfig config;

    /**
     * 分发服务器实例
     */
    private Map<String, DispatcherInstance> dispatcherInstanceMap = new ConcurrentHashMap<>();

    /**
     * 保存了连接地址
     */
    private Set<String> ipList = new HashSet<>();


    public DispatcherManager(AcceptorConfig config, SessionManagerFacade sessionManagerFacade) {
        this.config = config;
        this.sessionManagerFacade = sessionManagerFacade;
        this.acceptorInstanceId = StringUtils.getRandomString(16);
    }

    /**
     * 初始化zookeeper管理器
     */
    public void initialize() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework framework = CuratorFrameworkFactory.newClient(config.getZookeeperServer(), retryPolicy);
        framework.start();
        List<DispatcherInstanceAddress> dispatcherAddress = getDispatcherAddress(framework);
        if (!dispatcherAddress.isEmpty()) {
            for (DispatcherInstanceAddress address : dispatcherAddress) {
                connect(address);
            }
        }
        addWatcher(framework);
    }

    private void addWatcher(CuratorFramework framework) {
        try {
            framework.getChildren()
                    .usingWatcher((Watcher) watchedEvent -> {
                        Watcher.Event.EventType type = watchedEvent.getType();
                        if (type == Watcher.Event.EventType.NodeChildrenChanged) {
                            processDispatcherListChanged(framework);
                        }
                    }).forPath(Constants.ZK_DISPATCH_PATH);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 获取分发服务器IP地址
     *
     * @param framework 客户端
     * @return 分发服务器IP列表
     */
    private List<DispatcherInstanceAddress> getDispatcherAddress(CuratorFramework framework) {
        try {
            Stat stat = framework.checkExists().forPath(Constants.ZK_DISPATCH_PATH);
            if (stat == null) {
                framework.create().creatingParentsIfNeeded().forPath(Constants.ZK_DISPATCH_PATH);
            }
            List<String> children = framework.getChildren().forPath(Constants.ZK_DISPATCH_PATH);
            if (children.isEmpty()) {
                return new ArrayList<>();
            }
            List<DispatcherInstanceAddress> addresses = new ArrayList<>();
            for (String child : children) {
                DispatcherInstanceAddress address = parseAddress(child);
                addresses.add(address);
            }
            return addresses;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private DispatcherInstanceAddress parseAddress(String child) {
        String[] split = child.split(":");
        String ip = split[0];
        int port = Integer.valueOf(split[1]);
        return DispatcherInstanceAddress.builder()
                .ip(ip)
                .port(port)
                .build();
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
                            ch.pipeline().addLast(new DispatcherHandler(DispatcherManager.this,
                                    sessionManagerFacade, acceptorInstanceId));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect(address.getIp(), address.getPort()).sync();
            channelFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 对于ZK的数据而言，大概是这样的：
     * <p>
     * /phantom/dispatcher/
     * -----http://localhost:8019
     * -----http://localhost:8090
     * <p>
     * 节点路径表示ip地址，节点内容表示该节点目标有多少个客户端连接
     */
    private void processDispatcherListChanged(CuratorFramework framework) {
        try {
            List<String> children = framework.getChildren().forPath(Constants.ZK_DISPATCH_PATH);
            if (children.isEmpty()) {
                log.info("分发系统列表为空，清除内存缓存");
                ipList.clear();
                addWatcher(framework);
                return;
            }
            // handle new node
            for (String child : children) {
                if (!ipList.contains(child)) {
                    log.info("分发系统上线，添加地址：{} , 同时需要建立和分发系统的连接", child);
                    ipList.add(child);
                    DispatcherInstanceAddress dispatcherInstanceAddress = parseAddress(child);
                    connect(dispatcherInstanceAddress);
                }
            }

            // handle remove node
            Iterator<String> iterator = ipList.iterator();
            while (iterator.hasNext()) {
                String existsIP = iterator.next();
                if (!children.contains(existsIP)) {
                    log.info("分发系统下线，移除地址：{}", existsIP);
                    iterator.remove();
                }
            }
            addWatcher(framework);
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

    /**
     * 选择一个分发系统
     *
     * @param uid 用户ID
     * @return 分发系统实例
     */
    public DispatcherInstance chooseDispatcher(String uid) {
        ArrayList<DispatcherInstance> dispatcherInstances = new ArrayList<>(dispatcherInstanceMap.values());
        if (dispatcherInstances.isEmpty()) {
            return null;
        }
        int hash = toPositive(murmur2(uid.getBytes()));
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
