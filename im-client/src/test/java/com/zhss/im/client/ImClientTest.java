package com.zhss.im.client;

/**
 * @author Jianfeng Wang
 * @since 2019/11/13 11:41
 */
public class ImClientTest {


    public static void main(String[] args) {
        ImClient.getInstance().initialize();
        ImClient.getInstance().authenticate("test_uid_001", "test_token_001");
        ImClient.getInstance().sendMessage("test_uid_001", "test_uid_002", "hello world");
    }

}
