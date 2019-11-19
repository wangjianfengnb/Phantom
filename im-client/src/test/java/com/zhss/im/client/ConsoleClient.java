package com.zhss.im.client;

import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * @author Jianfeng Wang
 * @since 2019/11/13 11:41
 */
@Slf4j
public class ConsoleClient {


    public static void main(String[] args) {
        System.out.println("请输入用户名密码：[用户名]:[token]");
        Scanner scanner = new Scanner(System.in);
        String line = scanner.nextLine();
        String[] split = line.split(":");
        if (split.length != 2) {
            System.out.println("输入不合法！");
            return;
        }
        String uid = split[0];
        String token = split[1];
        ImClient.getInstance().initialize();
        ImClient.getInstance().authenticate(uid, token);

        ImClient.getInstance().addMessageListener(msg -> {
            log.info("收到消息：{}", msg);
        });

        System.out.println("*************************");
        System.out.println("******发送消息格式******");
        System.out.println("*****[uid]:[message]:[type 1单聊 2群聊]*******");
        System.out.println("*************************");
        System.out.println("*************************");
        while ((line = scanner.nextLine()) != null) {
            split = line.split(":");
            if (split.length != 3) {
                System.out.println("输入不合法！");
                continue;
            }
            String receiverId = split[0];
            String content = split[1];
            String type = split[2];
            if (type.equals("1")) {
                ImClient.getInstance().sendMessage(uid, receiverId, content);
            } else {
                ImClient.getInstance().sendGroupMessage(uid, receiverId, content);
            }
        }
    }

}
