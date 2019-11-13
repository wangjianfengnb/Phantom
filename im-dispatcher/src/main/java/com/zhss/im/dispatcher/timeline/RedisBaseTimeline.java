package com.zhss.im.dispatcher.timeline;

import com.zhss.im.common.Constants;
import com.zhss.im.common.model.PushMessage;
import com.zhss.im.dispatcher.config.DispatcherConfig;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

/**
 * 基于Redis的Timeline
 *
 * @author Jianfeng Wang
 * @since 2019/11/13 14:24
 */
@Slf4j
public class RedisBaseTimeline implements Timeline {

    private RedissonClient redissonClient;

    public RedisBaseTimeline(DispatcherConfig dispatcherConfig) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(dispatcherConfig.getRedisServer());
        this.redissonClient = Redisson.create(config);
    }


    /**
     * @param uid      用户ID
     * @param size     抓取记录条数
     * @param sequence 序列号
     */
    @Override
    public void fetchMessage(String uid, int size, long sequence) {

    }

    /**
     * @param uid         用户id
     * @param pushMessage 消息
     */
    @Override
    public void saveMessage(String uid, PushMessage pushMessage) {
        log.info("保存消息到timeline：{} -> {}", uid, pushMessage);
        long sequence = incrementSequence(uid);
        String receiverId = pushMessage.getReceiverId();
        String key = Constants.TIMELINE_PREFIX + receiverId;


    }

    /**
     * 获取用户消息序列号
     * <p>
     * 先从某个值开始取，如果数据丢失(被lru删除)，直接从timeline中获取最大的sequence.
     * <p>
     * 事实上，如果redis挂了，数据全部丢失了，此时会出现问题，可以设计一个epoch，类似版本号之类的，持久化到mysql中。
     * <p>
     * 客户端根据epoch和sequence来判断数据的连续完整性。
     *
     * @param uid 用户ID
     * @return 序列号
     */
    private long incrementSequence(String uid) {
        String key = Constants.MESSAGE_SEQUENCE_PREFIX + uid;
        RAtomicLong sequence = redissonClient.getAtomicLong(key);
        return sequence.incrementAndGet();
    }


}
