package com.phantom.dispatcher.message;

import com.google.protobuf.InvalidProtocolBufferException;
import com.phantom.common.AcceptorRegisterRequest;
import com.phantom.common.Message;
import com.phantom.common.util.NetUtils;
import com.phantom.dispatcher.acceptor.AcceptorInstance;
import com.phantom.dispatcher.acceptor.AcceptorServerManager;
import com.phantom.dispatcher.session.SessionManager;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 接入系统注册
 *
 * @author Jianfeng Wang
 * @since 2019/11/22 16:27
 */
@Slf4j
public class AcceptorRegisterMessageHandler extends AbstractMessageHandler<AcceptorRegisterRequest> {

    AcceptorRegisterMessageHandler(SessionManager sessionManager) {
        super(sessionManager);
    }

    @Override
    protected AcceptorRegisterRequest parseMessage(Message message) throws InvalidProtocolBufferException {
        return AcceptorRegisterRequest.parseFrom(message.getBody());
    }

    @Override
    protected void processMessage(AcceptorRegisterRequest message, SocketChannel channel) {
        String acceptorInstanceId = message.getAcceptorInstanceId();
        String channelId = NetUtils.getChannelId(channel);
        AcceptorInstance acceptorInstance = AcceptorInstance.builder()
                .channel(channel)
                .build();
        log.info("收到接入系统注册信息：{} -> {}", channelId, acceptorInstanceId);
        AcceptorServerManager.getInstance().addAcceptorInstance(acceptorInstanceId, acceptorInstance);
    }
}
