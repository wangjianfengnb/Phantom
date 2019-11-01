package com.zhss.im.dispatcher;

/**
 * 接入系统管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/1 18:22
 */
public class AcceptorServerManager {

    private static AcceptorServerManager instance = new AcceptorServerManager();

    public static AcceptorServerManager getInstance() {
        return instance;
    }

    private AcceptorServerManager() {
    }




}
