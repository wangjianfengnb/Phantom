package com.zhss.im.acceptor.server;

import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.session.SessionManagerFacade;
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

    public AcceptorHandler(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade) {
        super(dispatcherManager, sessionManagerFacade);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端建立连接 : {}", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        sessionManagerFacade.removeSession((SocketChannel) ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("发生异常：{}", cause.getMessage());
    }
}
