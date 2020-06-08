package com.phantom.acceptor.session;

import com.alibaba.fastjson.JSONObject;
import com.phantom.acceptor.config.AcceptorConfig;
import com.phantom.common.Constants;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.config.Config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.phantom.common.Constants.SESSION_PREFIX;

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

    /**
     * 用户会话，表示接入系统内存中保存和用户的连接
     */
    private Map<String, SocketChannel> uid2Channel = new ConcurrentHashMap<>();

    /**
     * channel对应用户ID
     */
    private Map<SocketChannel, String> channel2Uid = new ConcurrentHashMap<>();

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

    private Encoder encoder = in -> Unpooled.copiedBuffer(JSONObject.toJSONString(in).getBytes());

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


    public RedisSessionManager(AcceptorConfig acceptorConfig) {
        this.config = acceptorConfig;
        Config config = new Config();
        config.useClusterServers()
                .setPingConnectionInterval(60 * 1000)
                .addNodeAddress(this.config.getRedisServer().split(","));
        this.redissonClient = Redisson.create(config);
    }


    @Override
    public void addSession(String uid, Session session, SocketChannel channel) {
        uid2Channel.put(uid, channel);
        channel2Uid.put(channel, uid);
        String key = SESSION_PREFIX + uid;
        String value = JSONObject.toJSONString(session);
        RBucket<Session> bucket = redissonClient.getBucket(key, codec);
        bucket.set(session);
        log.info("往redis中写入session ：{} -> {}", key, value);
    }

    @Override
    public boolean removeSession(SocketChannel channel) {
        String uid = channel2Uid.remove(channel);
        if (uid != null) {
            uid2Channel.remove(uid);
            String sessionKey = Constants.SESSION_PREFIX + uid;
            RBucket<String> bucket = redissonClient.getBucket(sessionKey, codec);
            log.info("从redis中删除session: {}", bucket.get());
            bucket.delete();
        }
        return uid != null;
    }

    @Override
    public SocketChannel getChannel(String uid) {
        return uid2Channel.get(uid);
    }

    @Override
    public String getUid(SocketChannel channel) {
        return channel2Uid.get(channel);
    }

}
