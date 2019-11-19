package com.zhss.im.business.kafka;

import org.springframework.stereotype.Component;

/**
 * 基于Snowflake算法生成
 * <p>
 * 基于long 64位计算
 *
 * <p>     41位时间戳                                        datacenterId   workerId   sequence
 * <p>
 * // 0 | 0001100 10100010 10111110 10001001 01011100 00 | 10001 | 1 1001 | 0000 00000000
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 13:39
 */
@Component
public class SnowflakeIdWorker {

    /**
     * 机器ID
     */
    private long workerId = 1;
    /**
     * 机器ID位数
     */
    private long workderIdBits = 5L;
    /**
     * 数据中心
     */
    private long datacenterId = 1;
    /**
     * 数据中心位数
     */
    private long datecenterIdBits = 5L;
    /**
     * 序列号
     */
    private long sequence = 1;
    /**
     * 序列号位数
     */
    private long sequenceBits = 12L;
    /**
     * 序列号掩码
     */
    private long sequenceMask = ~(-1L << sequenceBits);
    /**
     * 开始时间戳 2019-01-01
     */
    private long twepoch = 1546272000L;

    /**
     * 上一次时间戳
     */
    private long lastTimestamp = -1;

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id for " + (
                    lastTimestamp - timestamp) + " milliseconds");
        }
        if (lastTimestamp == timestamp) {
            // the same milliseconds
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                // if current timestamp sequence is larger than 4096, generate next timestamp
                timestamp = nextMillis(lastTimestamp);
            }
        } else {
            sequence = 0;
        }
        lastTimestamp = timestamp;
        return (timestamp - twepoch) << (datecenterIdBits + workderIdBits + sequenceBits) |
                datacenterId << (workderIdBits + sequenceBits) |
                workerId << sequenceBits |
                sequence;
    }

    private long nextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }


}
