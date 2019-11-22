package com.phantom.dispatcher;

import com.phantom.dispatcher.config.DispatcherConfig;
import com.phantom.dispatcher.message.MessageHandlerFactory;
import com.phantom.dispatcher.server.DispatcherServer;
import com.phantom.dispatcher.session.RedisSessionManager;
import com.phantom.dispatcher.session.SessionManager;

/**
 * 分发服务器启动类
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 18:02
 */
public class Bootstrap {

    public static void main(String[] args) throws Exception {

        // 1. parse config
        DispatcherConfig config = DispatcherConfig.parse("server.properties");

        // 2. initialize sessionManager
        SessionManager sessionManager = new RedisSessionManager(config);

        // 3. initialize message handlers
        MessageHandlerFactory.initialize(sessionManager);

        // 4. initialize netty server
        DispatcherServer dispatcherServer = new DispatcherServer(config);
        dispatcherServer.initialize();
    }
}
