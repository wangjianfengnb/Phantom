package com.zhss.im.dispatcher;

import com.zhss.im.dispatcher.config.DispatcherConfig;
import com.zhss.im.dispatcher.server.DispatcherServer;
import com.zhss.im.dispatcher.session.RedisSessionManager;
import com.zhss.im.dispatcher.session.SessionManager;

/**
 * 分发服务器启动类
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 18:02
 */
public class Bootstrap {

    public static void main(String[] args) throws Exception {
        DispatcherConfig config = DispatcherConfig.parse("server.properties");

        SessionManager sessionManager = new RedisSessionManager(config);

        DispatcherServer dispatcherServer = new DispatcherServer(config, sessionManager);
        dispatcherServer.initialize();
    }
}
