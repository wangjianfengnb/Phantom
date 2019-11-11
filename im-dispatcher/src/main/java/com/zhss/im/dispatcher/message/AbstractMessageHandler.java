package com.zhss.im.dispatcher.message;

import com.zhss.im.dispatcher.config.Configurable;
import com.zhss.im.dispatcher.config.DispatcherConfig;
import com.zhss.im.dispatcher.server.ProcessorManager;
import com.zhss.im.dispatcher.server.ProcessorTask;
import com.zhss.im.dispatcher.session.SessionManager;

/**
 * 抽象消息处理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/11 15:53
 */
public abstract class AbstractMessageHandler implements MessageHandler {


    protected DispatcherConfig dispatcherConfig;
    protected ProcessorManager processorManager;
    protected SessionManager sessionManager;

    public AbstractMessageHandler(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.dispatcherConfig = getConfig(sessionManager);
        this.processorManager = new ProcessorManager(dispatcherConfig);
    }

    protected DispatcherConfig getConfig(SessionManager sessionManager) {
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
    protected void execute(String uid, ProcessorTask task) {
        processorManager.addTask(uid, task);
    }


}
