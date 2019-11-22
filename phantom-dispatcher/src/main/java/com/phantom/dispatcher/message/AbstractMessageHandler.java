package com.phantom.dispatcher.message;

import com.phantom.dispatcher.acceptor.AcceptorInstance;
import com.phantom.dispatcher.acceptor.AcceptorServerManager;
import com.phantom.dispatcher.session.Session;
import com.phantom.dispatcher.session.SessionManager;
import com.phantom.common.Constants;
import com.phantom.common.Message;
import com.phantom.dispatcher.config.Configurable;
import com.phantom.dispatcher.config.DispatcherConfig;
import com.phantom.dispatcher.server.ProcessorManager;
import com.phantom.dispatcher.server.ProcessorTask;
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
        try {
            processorManager.addTask(uid, task);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送消息到分发服务器
     *
     * @param uid     用户id
     * @param message 消息
     */
    void sendToAcceptor(String uid, Message message) {
        try {
            Session session = sessionManager.getSession(uid);
            if (session != null) {
                String acceptorChannelId = session.getAcceptorInstanceId();
                AcceptorServerManager acceptorServerManager = AcceptorServerManager.getInstance();
                AcceptorInstance acceptorInstance = acceptorServerManager.getAcceptorInstance(acceptorChannelId);
                while (acceptorInstance == null) {
                    // 假设分发系统重启了，此时会等待接入系统发起连接，注册
                    log.info("获取接入系统失败，阻塞一段时间后重新获取... " + acceptorChannelId);
                    Thread.sleep(100);
                    acceptorInstance = acceptorServerManager.getAcceptorInstance(acceptorChannelId);
                }
                log.info("将消息转发给接入系统：uid = {}, requestType = {}", uid,
                        Constants.requestTypeName(message.getRequestType()));
                acceptorInstance.getChannel().writeAndFlush(message.getBuffer());
            } else {
                log.error("无法找到session，发送消息到接入系统失败...{}", uid);
            }
        } catch (Exception e) {
            log.error("转发消息到接入系统发生异常：", e);
        }
    }
}
