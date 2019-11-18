package com.zhss.im.dispatcher.message;

import com.zhss.im.common.Message;
import com.zhss.im.dispatcher.acceptor.AcceptorInstance;
import com.zhss.im.dispatcher.acceptor.AcceptorServerManager;
import com.zhss.im.dispatcher.config.Configurable;
import com.zhss.im.dispatcher.config.DispatcherConfig;
import com.zhss.im.dispatcher.server.ProcessorManager;
import com.zhss.im.dispatcher.server.ProcessorTask;
import com.zhss.im.dispatcher.session.Session;
import com.zhss.im.dispatcher.session.SessionManager;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象消息处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 15:53
 */
@Slf4j
public abstract class AbstractMessageHandler implements MessageHandler {

    protected DispatcherConfig dispatcherConfig;
    protected ProcessorManager processorManager;
    protected SessionManager sessionManager;

    AbstractMessageHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.dispatcherConfig = getConfig(sessionManager);
        this.processorManager = new ProcessorManager(dispatcherConfig);
    }

    private DispatcherConfig getConfig(SessionManager sessionManager) {
        DispatcherConfig config = null;
        if (sessionManager instanceof Configurable) {
            config = ((Configurable) sessionManager).getConfig();
        }
        if (config == null) {
            throw new IllegalArgumentException("Cannot find dispatcher config.");
        }
        return config;
    }

    /**
     * 执行具体业务逻辑
     */
    void execute(String uid, ProcessorTask task) {
        processorManager.addTask(uid, task);
    }

    /**
     * 发送消息到分发服务器
     *
     * @param uid     用户id
     * @param message 消息
     */
    void sendToAcceptor(String uid, Message message) {
        Session session = sessionManager.getSession(uid);
        if (session != null) {
            String acceptorChannelId = session.getAcceptorChannelId();
            AcceptorInstance acceptorInstance =
                    AcceptorServerManager.getInstance().getAcceptorInstance(acceptorChannelId);
            if (acceptorInstance != null) {
                log.info("将消息转发给接入系统：uid = {}, requestType = {}", uid, message.getRequestType());
                acceptorInstance.getChannel().writeAndFlush(message.getBuffer());
            } else {
                log.error("获取接入服务器channel失败....");
            }
        } else {
            log.error("无法找到session，发送消息到接入系统失败...");
        }
    }
}
