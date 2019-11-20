package com.zhss.im.acceptor.server;

import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.acceptor.zookeeper.ZookeeperManager;
import com.zhss.im.common.Constants;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.net.InetAddress;

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
        this.zookeeperManager.decrementClient();
        sessionManagerFacade.removeSession((SocketChannel) ctx.channel());
    }
}
