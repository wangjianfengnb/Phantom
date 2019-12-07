package com.phantom.common.util;


import io.netty.channel.socket.SocketChannel;

/**
 * 网络工具类
 *
 * @author Jianfeng Wang
 * @since 2019/11/7 16:00
 */
public class NetUtils {

    /**
     * 获取channel的id
     */
    public static String getChannelId(SocketChannel channel) {
        return channel.remoteAddress().getAddress() + ":" + channel.remoteAddress().getPort();
    }
}
