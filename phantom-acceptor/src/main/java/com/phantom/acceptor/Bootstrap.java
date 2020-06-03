package com.phantom.acceptor;

import com.phantom.acceptor.config.AcceptorConfig;
import com.phantom.acceptor.message.MessageHandlerFactory;
import com.phantom.acceptor.server.AcceptorServer;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.RedisSessionManager;
import com.phantom.acceptor.session.SessionManager;
import com.phantom.acceptor.zookeeper.ZookeeperManager;
import lombok.extern.slf4j.Slf4j;

/**
 * IM 接入服务启动类
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:36
 */
@Slf4j
public class Bootstrap {

    public static void main(String[] args) throws Exception {
        // 1. parse config
        AcceptorConfig config = AcceptorConfig.parse("server.properties");

        // 2. init session manager
        SessionManager sessionManager = new RedisSessionManager(config);

        // 3. init dispatcherManager
        DispatcherManager dispatcherManager = new DispatcherManager(config, sessionManager);
        dispatcherManager.initialize();

        // 4. init handlers
        ZookeeperManager zookeeperManager = new ZookeeperManager(config);
        MessageHandlerFactory.initialize(dispatcherManager, sessionManager, zookeeperManager, config);


        // 5. init acceptor server
        AcceptorServer server = new AcceptorServer(dispatcherManager, config, sessionManager);
        server.initialize(zookeeperManager);
    }
}
