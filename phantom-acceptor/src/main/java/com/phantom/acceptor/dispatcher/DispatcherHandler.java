package com.phantom.acceptor.dispatcher;

import com.phantom.acceptor.server.AbstractChannelHandler;
import com.phantom.acceptor.session.SessionManagerFacade;
import com.phantom.common.AcceptorRegisterRequest;
import com.phantom.common.Message;
import com.phantom.common.util.NetUtils;
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

    private String acceptorInstanceId;

    DispatcherHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade,
                      String acceptorInstanceId) {
        super(dispatcherManager, sessionManagerFacade);
        this.acceptorInstanceId = acceptorInstanceId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SocketChannel channel = (SocketChannel) ctx.channel();
        String instanceId = NetUtils.getChannelId(channel);
        DispatcherInstance instance = DispatcherInstance.builder()
                .channel(channel)
                .build();
        dispatcherManager.addDispatcherInstance(instanceId, instance);
        log.info("和分发系统连接建立,发送注册请求：{}", acceptorInstanceId);
        AcceptorRegisterRequest request = AcceptorRegisterRequest.newBuilder()
                .setAcceptorInstanceId(acceptorInstanceId)
                .build();
        Message message = Message.buildAcceptorRegisterRequest(request);
        ctx.writeAndFlush(message.getBuffer());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String instanceId = NetUtils.getChannelId((SocketChannel) ctx.channel());
        log.info("和分发系统连接断开: {}", instanceId);
        dispatcherManager.removeDispatcherInstance(instanceId);
    }

}
