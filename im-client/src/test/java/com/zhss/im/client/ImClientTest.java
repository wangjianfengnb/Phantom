package com.zhss.im.client;

import java.util.Scanner;

/**
 * @author Jianfeng Wang
 * @since 2019/11/13 11:41
 */
public class ImClientTest {


    public static void main(String[] args) {
        String uid = "test_uid_002";
        String token = "test_token";
        ImClient.getInstance().initialize();
        ImClient.getInstance().authenticate(uid, token);
        System.out.println("我是" + uid);
        Scanner input = new Scanner(System.in);

        while (true) {
            System.out.println("请输入要发送的对象：");
            String toUid = input.nextLine();
            System.out.println("toUid = " + toUid);
            if (toUid.equals("")) {
                continue;
            }

            System.out.println("请输入要发送的内容：");
            String content = input.nextLine();
            if (content.equals("")) {
                continue;
            }
            ImClient.getInstance().sendMessage(uid, toUid, content);
        }
    }

}
