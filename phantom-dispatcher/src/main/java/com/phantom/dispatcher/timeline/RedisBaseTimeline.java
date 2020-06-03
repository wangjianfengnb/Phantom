package com.phantom.dispatcher.timeline;

import com.phantom.common.Constants;
import com.phantom.dispatcher.config.DispatcherConfig;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 基于Redis的Timeline
 *
 * @author Jianfeng Wang
 * @since 2019/11/13 14:24
 */
@Slf4j
public class RedisBaseTimeline implements Timeline {

    private RedissonClient redissonClient;

    private DispatcherConfig dispatcherConfig;

    public RedisBaseTimeline(DispatcherConfig dispatcherConfig) {
        this.dispatcherConfig = dispatcherConfig;
        Config config = new Config();
        config.useClusterServers().addNodeAddress(dispatcherConfig.getRedisServer().split(","));
        this.redissonClient = Redisson.create(config);
    }


    @Override
    public List<TimelineMessage> fetchMessage(FetchRequest request) {
        String key = generateKey(request.getUid());
        RScoredSortedSet<TimelineMessage> timeline = redissonClient.getScoredSortedSet(key);
        // 根据timestamp获取数据
        Collection<TimelineMessage> timelineMessages = timeline.valueRange(request.getTimestamp(), false,
                Double.POSITIVE_INFINITY, true, 0, request.getSize());

        // 维护platform抓取数据的timestamp
        RMap<Integer, Long> platformStampMap =
                redissonClient.getMap(Constants.TIMELINE_TIMESTAMP_PREFIX + request.getUid());
        platformStampMap.put(request.getPlatform(), request.getTimestamp());
        // 获取各个平台最小的timestamp，按照最小的timestamp删除离线消息
        Collection<Long> allPlatformStamp = platformStampMap.values();
        long minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;
        for (Long time : allPlatformStamp) {
            minTimestamp = Math.min(time, minTimestamp);
            maxTimestamp = Math.max(time, maxTimestamp);
        }

        // 判断最大和最小的timestamp之间消息是否超过阈值,如果超过某个阈值，就只保留limit条消息
        int count = timeline.count(minTimestamp, true, maxTimestamp, true);
        int limit = dispatcherConfig.getMaxOfflineLag();
        if (count > limit) {
            // 从最大的timestamp往前推limit条消息，取1条消息，这条消息之前的就是要删除的消息
            Collection<TimelineMessage> targetMessage = timeline.valueRangeReversed(maxTimestamp, true,
                    Double.NEGATIVE_INFINITY, true, limit, 1);
            if (targetMessage != null && !targetMessage.isEmpty()) {
                TimelineMessage timelineMessage = targetMessage.stream().findFirst().get();
                minTimestamp = timelineMessage.getTimestamp();
            }
        }
        int removed = timeline.removeRangeByScore(Double.NEGATIVE_INFINITY, true, minTimestamp, true);
        log.info("移除【{}】之前的消息{}条", minTimestamp, removed);
        if (timelineMessages != null && !timelineMessages.isEmpty()) {
            return new ArrayList<>(timelineMessages);
        }
        return new ArrayList<>();

    }

    @Override
    public void saveMessage(TimelineMessage timelineMessage) {
        String uid = timelineMessage.getReceiverId();
        long sequence = incrementSequence(uid);
        timelineMessage.setSequence(sequence);
        String receiverId = timelineMessage.getReceiverId();
        String key = generateKey(receiverId);
        RScoredSortedSet<TimelineMessage> timeline = redissonClient.getScoredSortedSet(key);
        log.info("保存消息到timeline：{} -> {}", uid, timelineMessage);
        // 按时间顺序加入
        timeline.add(timelineMessage.getTimestamp(), timelineMessage);
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

    private String generateKey(String uid) {
        return Constants.TIMELINE_PREFIX + uid;
    }


}
