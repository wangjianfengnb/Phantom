package com.zhss.im.acceptor.session;

import com.zhss.im.acceptor.config.AcceptorConfig;
import com.zhss.im.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * 保存到redis的会话管理器
 *
 * @author Jianfeng Wang
 * @since 2019/11/8 14:43
 */
@Slf4j
public class RedisSessionManager implements SessionManager {
    /**
     * 配置
     */
    private AcceptorConfig config;

    /**
     * 操作redis
     */
    private RedissonClient redissonClient;

    RedisSessionManager(AcceptorConfig acceptorConfig) {
        this.config = acceptorConfig;
        Config config = new Config();
        config.useSingleServer()
                .setAddress(acceptorConfig.getRedisServer());
        this.redissonClient = Redisson.create(config);
    }

    @Override
    public void removeSession(String uid) {
        String sessionKey = Constants.SESSION_PREFIX + uid;
        RBucket<String> bucket = redissonClient.getBucket(sessionKey);
        log.info("从redis中删除session: {}", bucket.get());
        bucket.delete();
    }

}
