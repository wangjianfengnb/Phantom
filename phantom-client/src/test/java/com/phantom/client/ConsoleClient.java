package com.phantom.client;

import com.alibaba.fastjson.JSONObject;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Scanner;

/**
 * @author Jianfeng Wang
 * @since 2019/11/13 11:41
 */
@Slf4j
public class ConsoleClient {


    public static void main(String[] args) {
        System.out.println("                  简易客户端                  ");
        System.out.println();
        System.out.println("                  发送消息格式                     ");
        System.out.println();
        System.out.println("            [用户名]:[消息]:[消息类型]         ");
        System.out.println();
        System.out.println("   消息类型枚举：1-单聊消息(默认，可以不填)  2-群聊消息   ");
        System.out.println();
        System.out.println();
        System.out.println("eg : 发送给用户，用户ID为 uid_02，内容为“haha”       ->  输入：uid_02:haha [enter]");
        System.out.println("eg : 发送给群组，群组ID为 group_id_01，内容为“haha”  ->  输入：group_id_01:haha:2 [enter]");
        System.out.println();
        System.out.println("[注意] 群聊需要先通过Restful API建立群");
        System.out.println();

        System.out.println();
        System.out.println("请输入用户名密码：格式 [用户名]:[秘钥]  (用户名和秘钥随意输入) eg: 1:1 [enter]");
        System.out.println();

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

        while ((line = scanner.nextLine()) != null) {
            split = line.split(":");
            if (split.length < 2) {
                System.out.println("输入不合法！");
                continue;
            }
            String receiverId = split[0];
            String content = split[1];
            String type;
            if (split.length < 3 || StringUtil.isNullOrEmpty(split[2])) {
                type = "1";
            } else {
                type = split[2];
            }
            JSONObject obj = new JSONObject(1);
            obj.put("type", 1);
            obj.put("content", content);
            if (type.equals("1")) {
                ImClient.getInstance().sendMessage(uid, receiverId, obj.toJSONString());
            } else if (type.equals("2")) {
                ImClient.getInstance().sendGroupMessage(uid, receiverId, obj.toJSONString());
            }
        }
    }

}
