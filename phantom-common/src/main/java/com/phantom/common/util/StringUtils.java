package com.phantom.common.util;

import lombok.NonNull;

import java.util.Iterator;
import java.util.List;
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

    public static String join(@NonNull CharSequence delimiter, @NonNull Object[] tokens) {
        final int length = tokens.length;
        if (length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(tokens[0]);
        for (int i = 1; i < length; i++) {
            sb.append(delimiter);
            sb.append(tokens[i]);
        }
        return sb.toString();
    }

    public static String join(@NonNull CharSequence delimiter, @NonNull Iterable tokens) {
        final Iterator<?> it = tokens.iterator();
        if (!it.hasNext()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(delimiter);
            sb.append(it.next());
        }
        return sb.toString();
    }
}
