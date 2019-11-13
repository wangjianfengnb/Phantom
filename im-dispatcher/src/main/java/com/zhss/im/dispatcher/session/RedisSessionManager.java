package com.zhss.im.dispatcher.session;

import com.alibaba.fastjson.JSONObject;
import com.zhss.im.dispatcher.config.Configurable;
import com.zhss.im.dispatcher.config.DispatcherConfig;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

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
    private RedissonClient redissonClient;

    public RedisSessionManager(DispatcherConfig dispatcherConfig) {
        this.config = dispatcherConfig;
        Config config = new Config();
        config.useSingleServer()
                .setAddress(dispatcherConfig.getRedisServer());
        this.redissonClient = Redisson.create(config);
    }


    @Override
    public void addSession(String uid, Session session) {
        String key = SESSION_PREFIX + uid;
        String value = JSONObject.toJSONString(session);
        RBucket<Session> bucket = redissonClient.getBucket(key);
        bucket.set(session);
        log.info("往redis中写入session ：{} -> {}", key, value);
    }

    @Override
    public void removeSession(String uid) {

    }

    @Override
    public Session getSession(String uid) {
        String key = SESSION_PREFIX + uid;
        RBucket<Session> bucket = redissonClient.getBucket(key);
        return bucket.get();
    }

    @Override
    public DispatcherConfig getConfig() {
        return config;
    }
}
