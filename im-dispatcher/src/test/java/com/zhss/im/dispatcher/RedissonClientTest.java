package com.zhss.im.dispatcher;

import com.zhss.im.dispatcher.timeline.TimelineMessage;
import org.redisson.Redisson;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.protocol.ScoredEntry;
import org.redisson.config.Config;

import java.util.Collection;

/**
 * @author Jianfeng Wang
 * @since 2019/11/18 11:24
 */
public class RedissonClientTest {

    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://redis-service:6379");
        RedissonClient redissonClient = Redisson.create(config);

        RScoredSortedSet<TimelineMessage> timelineMessages =
                redissonClient.getScoredSortedSet("test_score_set");

        addTestMessage(timelineMessages);

        Collection<TimelineMessage> timelineMessages3 = timelineMessages.valueRange(3, false,
                Double.POSITIVE_INFINITY, true,
                0, 2);
        System.out.println("从分数3开始取两条:" + timelineMessages3);


        Collection<TimelineMessage> timelineMessages1 = timelineMessages.valueRange(0, 2);
        System.out.println("获取0-2消息：" + timelineMessages1);
        timelineMessages.removeRangeByRankAsync(0, 2);
        Collection<TimelineMessage> timelineMessages2 = timelineMessages.readAll();

        System.out.println("删除0-2消息后的数据：" + timelineMessages2);

        Collection<ScoredEntry<TimelineMessage>> scoredEntries = timelineMessages.entryRange(0,
                -1);

        for (ScoredEntry<TimelineMessage> entry : scoredEntries) {

            System.out.println("集合：" + entry.getScore() + " -> " + entry.getValue());
        }
        timelineMessages.delete();
    }

    private static void addTestMessage(RScoredSortedSet<TimelineMessage> timelineMessages) {
        for (int i = 0; i < 10; i++) {
            TimelineMessage build = TimelineMessage.builder()
                    .senderId("test001")
                    .receiverId("test002")
                    .messageType(1)
                    .content("hello world " + i)
                    .timestamp(i)
                    .sequence(i)
                    .build();
            timelineMessages.add(build.getTimestamp(), build);
        }
    }

}
