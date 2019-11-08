package com.zhss.im.acceptor;

import com.zhss.im.acceptor.config.AcceptorConfig;
import com.zhss.im.acceptor.dispatcher.DispatcherManager;
import com.zhss.im.acceptor.server.AcceptorServer;
import com.zhss.im.acceptor.session.SessionManagerFacade;
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
        AcceptorConfig config = AcceptorConfig.parse("server.properties");
        SessionManagerFacade sessionManagerFacade = new SessionManagerFacade(config);

        DispatcherManager dispatcherManager = new DispatcherManager(config, sessionManagerFacade);
        dispatcherManager.initialize();


        AcceptorServer server = new AcceptorServer(dispatcherManager, config, sessionManagerFacade);
        server.initialize();
    }
}
