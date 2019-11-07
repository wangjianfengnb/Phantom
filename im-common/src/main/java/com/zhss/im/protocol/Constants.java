package com.zhss.im.protocol;

/**
 * 常量表
 */
public class Constants {

    /**
     * APP SDK版本号
     */
    public static final int APP_SDK_VERSION_1 = 1;
    /**
     * 消息类型：请求
     */
    public static final int MESSAGE_TYPE_REQUEST = 1;
    /**
     * 消息类型：响应
     */
    public static final int MESSAGE_TYPE_RESPONSE = 2;
    /**
     * 请求类型：用户认证
     */
    public static final int REQUEST_TYPE_AUTHENTICATE = 1;
    /**
     * 每条消息的分隔符
     */
    public static final byte[] DELIMITER = "$_".getBytes();
    /**
     * 响应状态：正常
     */
    public static final int RESPONSE_STATUS_OK = 1;
    /**
     * 响应状态：异常
     */
    public static final int RESPONSE_STATUS_ERROR = 2;
    /**
     * 消息头长度
     */
    public static final int HEADER_LENGTH = 20;


}
