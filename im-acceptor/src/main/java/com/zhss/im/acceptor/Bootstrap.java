package com.zhss.im.acceptor;

import com.zhss.im.acceptor.config.AcceptorConfig;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.message.MessageHandlerFactory;
import com.zhss.im.acceptor.server.AcceptorServer;
import com.zhss.im.acceptor.session.SessionManagerFacade;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

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
        SessionManagerFacade sessionManagerFacade = new SessionManagerFacade(config);

        // 3. init dispatcherManager
        DispatcherManager dispatcherManager = new DispatcherManager(config, sessionManagerFacade);
        dispatcherManager.initialize();

        // 4. init handlers
        MessageHandlerFactory.initialize(dispatcherManager, sessionManagerFacade);


        // 5. init acceptor server
        AcceptorServer server = new AcceptorServer(dispatcherManager, config, sessionManagerFacade);
        server.initialize();
    }
}
