package com.zhss.im.acceptor.session;

import com.zhss.im.acceptor.config.AcceptorConfig;
import com.zhss.im.common.Constants;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
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

    private Decoder<Object> decoder = (buf, state) -> {
        if (buf == null) {
            return null;
        }
        int length = buf.readableBytes();

        if (length == 0) {
            return null;
        }
        byte[] data = new byte[length];
        buf.readBytes(data);
        return new String(data, 0, length);
    };

    private Encoder encoder = in -> Unpooled.copiedBuffer("".getBytes());

    private Codec codec = new BaseCodec() {
        @Override
        public Decoder<Object> getValueDecoder() {
            return decoder;
        }

        @Override
        public Encoder getValueEncoder() {
            return encoder;
        }
    };


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
        RBucket<String> bucket = redissonClient.getBucket(sessionKey, codec);
        log.info("从redis中删除session: {}", bucket.get());
        bucket.delete();
    }

}
