package com.zhss.im.acceptor.dispatcher;

import com.zhss.im.acceptor.server.AbstractChannelHandler;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import com.zhss.im.protocol.NetUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 分发系统建立连接处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/7 15:56
 */
@Slf4j
public class DispatcherHandler extends AbstractChannelHandler {

    DispatcherHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        super(dispatcherManager, sessionManagerFacade);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String instanceId = NetUtils.getInstanceId(channel);
        DispatcherInstance instance = DispatcherInstance.builder()
                .channel(channel)
                .build();
        log.info("接入系统和分发系统连接建立: {}", instanceId);
        dispatcherManager.addDispatcherInstance(instanceId, instance);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String instanceId = NetUtils.getInstanceId((SocketChannel) ctx.channel());
        log.info("接入系统和分发系统连接断开: {}", instanceId);
        dispatcherManager.removeDispatcherInstance(instanceId);
    }
}
