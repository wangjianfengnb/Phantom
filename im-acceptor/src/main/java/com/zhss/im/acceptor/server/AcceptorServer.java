package com.zhss.im.acceptor.server;

import com.zhss.im.acceptor.config.AcceptorConfig;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.acceptor.zookeeper.ZookeeperManager;
import com.zhss.im.common.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.net.InetAddress;

/**
 * 接入系统Netty服务端
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 15:43
 */
@Slf4j
public class AcceptorServer {

    private DispatcherManager dispatcherManager;
    private AcceptorConfig config;
    private SessionManagerFacade sessionManagerFacade;

    public AcceptorServer(DispatcherManager dispatcherManager, AcceptorConfig config,
                          SessionManagerFacade sessionManagerFacade) {
        this.config = config;
        this.sessionManagerFacade = sessionManagerFacade;
        this.dispatcherManager = dispatcherManager;
    }

    /**
     * 初始化服务器
     */
    public void initialize() {
        EventLoopGroup connectThreadGroup = new NioEventLoopGroup();
        EventLoopGroup ioThreadGroup = new NioEventLoopGroup();
        try {
            ZookeeperManager zookeeperManager = new ZookeeperManager(config);
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(connectThreadGroup, ioThreadGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(config.getMaxMessageBytes(),
                                    Unpooled.copiedBuffer(Constants.DELIMITER)));
                            socketChannel.pipeline().addLast(new AcceptorHandler(dispatcherManager,
                                    sessionManagerFacade, zookeeperManager));
                        }
                    });
            log.info("接入服务初始化......监听端口：{}", config.getPort());
            ChannelFuture channelFuture = bootstrap.bind(config.getPort()).sync();
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> zookeeperManager.createNode());
            channelFuture.sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
