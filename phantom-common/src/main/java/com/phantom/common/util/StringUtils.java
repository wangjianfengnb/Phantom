package com.phantom.common.util;

import java.util.Random;

/**
 * 字符串工具类
 *
 * @author Jianfeng Wang
 * @since 2019/11/22 16:10
 */
public class StringUtils {

    /**
     * 判断是否为空
     *
     * @param s 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    /**
     * 判断是否非空
     *
     * @param s 字符串
     * @return 是否非空
     */

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }


    /**
     * 获取指定长度随机字符串
     *
     * @param length 长度
     * @return 随机字符串
     */
    public static String getRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(3);
            long result = 0;
            switch (number) {
                case 0:
                    result = Math.round(Math.random() * 25 + 65);
                    sb.append((char) result);
                    break;
                case 1:
                    result = Math.round(Math.random() * 25 + 97);
                    sb.append((char) result);
                    break;
                case 2:
                    sb.append(new Random().nextInt(10));
                    break;
                default:
                    break;
            }
        }
        return sb.toString();
    }


}
