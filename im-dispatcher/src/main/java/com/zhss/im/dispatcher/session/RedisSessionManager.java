package com.zhss.im.dispatcher.session;

import com.alibaba.fastjson.JSONObject;
import com.zhss.im.dispatcher.config.Configurable;
import com.zhss.im.dispatcher.config.DispatcherConfig;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import static com.zhss.im.common.Constants.SESSION_PREFIX;

/**
 * 客户端会话门面
 *
 * @author Jianfeng Wang
 * @since 2019/10/28 21:38
 */
@Slf4j
public class RedisSessionManager implements SessionManager, Configurable {

    private DispatcherConfig config;
    private Jedis jedis;

    public RedisSessionManager(DispatcherConfig config) {
        this.config = config;
        this.jedis = new Jedis(config.getRedisServer());
    }


    @Override
    public void addSession(String uid, Session session) {
        String key = SESSION_PREFIX + uid;
        String value = JSONObject.toJSONString(session);
        jedis.set(key, value);
        log.info("往redis中写入session ：{} -> {}", key, value);
    }

    @Override
    public void removeSession(String uid) {

    }

    @Override
    public Session getSession(String uid) {
        String key = SESSION_PREFIX + uid;
        String value = jedis.get(key);
        if (value == null) {
            return null;
        }
        return JSONObject.parseObject(value, Session.class);
    }

    @Override
    public DispatcherConfig getConfig() {
        return config;
    }
}
