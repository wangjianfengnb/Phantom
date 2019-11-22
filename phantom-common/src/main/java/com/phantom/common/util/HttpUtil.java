package com.phantom.common.util;

import com.sun.deploy.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * http网络请求工具类
 *
 * @author Jianfeng Wang
 * @since 2019/7/24 13:54
 */
public class HttpUtil {

    public static byte[] getRawBytes(String url, Map<String, String> params) {
        try {
            if (params != null) {
                List<String> p = new ArrayList<>();
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    p.add(entry.getKey() + "=" + entry.getValue());
                }
                String join = StringUtils.join(p, "&");
                url = url + "?" + join;
            }
            URL conURL = new URL(url);
            System.setProperty("java.protocol.handler.pkgs", "javax.net.ssl");
            HostnameVerifier hv = (urlHostName, session) -> urlHostName.equals(session.getPeerHost());
            HttpsURLConnection.setDefaultHostnameVerifier(hv);
            HttpURLConnection conn = (HttpURLConnection) conURL.openConnection();
            conn.setDoInput(true);
            conn.setReadTimeout(60000);
            conn.setConnectTimeout(30000);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return null;
            }
            InputStream in = conn.getInputStream();
            int len;
            byte[] buf = new byte[4096];
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            while ((len = in.read(buf)) > 0) {
                bao.write(buf, 0, len);
            }
            in.close();
            return bao.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static String get(String url, Map<String, String> params) {
        byte[] rawBytes = getRawBytes(url, params);
        if (rawBytes == null) {
            return null;
        }
        return new String(rawBytes, StandardCharsets.UTF_8);
    }
}
