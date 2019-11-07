package com.zhss.im.acceptor.dispatcher;

/**
 * 分发系统实例
 * @author Jianfeng Wang
 */
public class DispatcherInstanceAddress {

    private String host;
    private String ip;
    private int port;

    public DispatcherInstanceAddress(String host, String ip, int port) {
        this.host = host;
        this.ip = ip;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

}
