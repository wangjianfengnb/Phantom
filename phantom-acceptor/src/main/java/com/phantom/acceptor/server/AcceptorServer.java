package com.phantom.acceptor.server;

import com.phantom.acceptor.config.AcceptorConfig;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.server.ssl.SslEngineFactory;
import com.phantom.acceptor.session.SessionManager;
import com.phantom.acceptor.zookeeper.ZookeeperManager;
import com.phantom.common.Constants;
import com.phantom.common.util.StringUtils;
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
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLEngine;

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
    private SessionManager sessionManager;

    public AcceptorServer(DispatcherManager dispatcherManager, AcceptorConfig config,
                          SessionManager sessionManager) {
        this.config = config;
        this.sessionManager = sessionManager;
        this.dispatcherManager = dispatcherManager;
    }

    /**
     * 初始化服务器
     *
     * @param zookeeperManager zookeeper管理
     */
    public void initialize(ZookeeperManager zookeeperManager) {
        EventLoopGroup connectThreadGroup = new NioEventLoopGroup();
        EventLoopGroup ioThreadGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(connectThreadGroup, ioThreadGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            if (StringUtils.isNotEmpty(config.getKeyStore())) {
                                SSLEngine engine = SslEngineFactory.getEngine(config);
                                if (engine != null) {
                                    socketChannel.pipeline().addLast(new SslHandler(engine));
                                } else {
                                    log.error("初始化SSL上下文失败");
                                }
                            }
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(config.getMaxMessageBytes(),
                                    Unpooled.copiedBuffer(Constants.DELIMITER)));
                            socketChannel.pipeline().addLast(new AcceptorHandler(dispatcherManager,
                                    sessionManager, zookeeperManager));
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
