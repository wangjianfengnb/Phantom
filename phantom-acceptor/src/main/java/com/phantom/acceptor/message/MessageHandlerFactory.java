package com.phantom.acceptor.message;

import com.phantom.acceptor.config.AcceptorConfig;
import com.phantom.acceptor.dispatcher.DispatcherManager;
import com.phantom.acceptor.session.SessionManagerFacade;
import com.phantom.acceptor.zookeeper.ZookeeperManager;
import com.phantom.common.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息处理器工厂
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 10:55
 */
public class MessageHandlerFactory {


    private static Map<Integer, MessageHandler> handlers = new HashMap<>();

    public static void initialize(DispatcherManager dispatcherManager, SessionManagerFacade sessionManagerFacade,
                                  ZookeeperManager zookeeperManager) {
        AcceptorConfig acceptorConfig = sessionManagerFacade.getAcceptorConfig();
        AtomicInteger count = new AtomicInteger();
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(acceptorConfig.getCoreSize(),
                acceptorConfig.getCoreSize(),
                0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), r -> {
            Thread t = new Thread(r);
            t.setName("Acceptor-IO-Thread-" + count.incrementAndGet());
            return t;
        });
        handlers.put(Constants.REQUEST_TYPE_AUTHENTICATE, new AuthenticateMessageHandler(dispatcherManager,
                sessionManagerFacade, threadPoolExecutor, zookeeperManager));
        handlers.put(Constants.REQUEST_TYPE_C2C_SEND, new C2cMessageHandler(dispatcherManager, sessionManagerFacade,
                threadPoolExecutor));
        handlers.put(Constants.REQUEST_TYPE_INFORM_FETCH, new InformFetcherMessageHandler(dispatcherManager,
                sessionManagerFacade, threadPoolExecutor));
        handlers.put(Constants.REQUEST_TYPE_MESSAGE_FETCH, new FetchMessageHandler(dispatcherManager,
                sessionManagerFacade, threadPoolExecutor));
        handlers.put(Constants.REQUEST_TYPE_C2G_SEND, new C2gMessageHandler(dispatcherManager, sessionManagerFacade,
                threadPoolExecutor));
    }


    /**
     * 根据请求类型获取消息处理器
     *
     * @param requestType 请求类型
     * @return 消息处理器
     */
    public static MessageHandler getMessageHandler(int requestType) {
        return handlers.get(requestType);
    }


}
