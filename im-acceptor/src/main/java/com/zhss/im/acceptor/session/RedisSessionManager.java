package com.zhss.im.acceptor.session;

import com.zhss.im.acceptor.config.AcceptorConfig;
import com.zhss.im.protocol.Constants;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

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
    private Jedis jedis;

    public RedisSessionManager(AcceptorConfig config) {
        this.config = config;
        this.jedis = new Jedis(config.getRedisServer());
    }

    @Override
    public void removeSession(String uid) {
        String sessionKey = Constants.SESSION_PREFIX + uid;
        String sessionValue = jedis.get(sessionKey);
        jedis.del(sessionKey);
        log.info("从redis中删除session: {}", sessionValue);
    }
}
