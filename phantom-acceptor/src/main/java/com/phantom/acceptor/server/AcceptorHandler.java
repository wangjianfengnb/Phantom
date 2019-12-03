package com.phantom.acceptor.server;

import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManagerFacade;
import com.phantom.acceptor.zookeeper.ZookeeperManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 处理连接
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:50
 */
@Slf4j
public class AcceptorHandler extends AbstractChannelHandler {

    private ZookeeperManager zookeeperManager;

    public AcceptorHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade,
                           ZookeeperManager zookeeperManager) {
        super(dispatcherManager, sessionManagerFacade);
        this.zookeeperManager = zookeeperManager;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端建立连接 : {}", ctx.channel());
        this.zookeeperManager.incrementClient();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端断开连接：{}", ctx.channel());
        this.zookeeperManager.decrementClient();
        sessionManagerFacade.removeSession((SocketChannel) ctx.channel());
    }
}
