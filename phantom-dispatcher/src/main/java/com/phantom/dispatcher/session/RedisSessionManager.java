package com.phantom.dispatcher.session;

import com.alibaba.fastjson.JSONObject;
import com.phantom.dispatcher.config.Configurable;
import com.phantom.dispatcher.config.DispatcherConfig;
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

import static com.phantom.common.Constants.SESSION_PREFIX;

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

    private Decoder<Object> decoder = (buf, state) -> {
        int length = buf.readableBytes();
        byte[] data = new byte[length];
        buf.readBytes(data);
        String s = new String(data, 0, length);
        return JSONObject.parseObject(s, Session.class);
    };

    private Encoder encoder = in -> {
        String s = JSONObject.toJSONString(in);
        return Unpooled.copiedBuffer(s.getBytes());
    };

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


    public RedisSessionManager(DispatcherConfig dispatcherConfig) {
        this.config = dispatcherConfig;
        Config config = new Config();
        config.useClusterServers()
                .setPingConnectionInterval(60 * 1000)
                .addNodeAddress(this.config.getRedisServer().split(","));
        this.redissonClient = Redisson.create(config);
    }

    @Override
    public Session getSession(String uid) {
        String key = SESSION_PREFIX + uid;
        RBucket<Session> bucket = redissonClient.getBucket(key, codec);
        return bucket.get();
    }

    @Override
    public DispatcherConfig getConfig() {
        return config;
    }
}
