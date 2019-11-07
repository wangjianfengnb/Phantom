package com.zhss.im.acceptor;

import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.protocol.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * IM 接入服务启动类
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:36
 */
@Slf4j
public class AcceptorServerBootstrap {

    public static final int PORT = 8080;

    public static void main(String[] args) {
        DispatcherManager.getInstance().initialize();
        EventLoopGroup connectThreadGroup = new NioEventLoopGroup();
        EventLoopGroup ioThreadGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(connectThreadGroup, ioThreadGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(4096,
                                    Unpooled.copiedBuffer(Constants.DELIMITER)));
                            socketChannel.pipeline().addLast(new AcceptorHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(PORT).sync();
            channelFuture.sync();
            log.info("接入服务初始化完毕...监听端口：{}",PORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
