package com.phantom.business.mapper;

import com.phantom.common.model.KafkaMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 群聊消息
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 17:48
 */
@Mapper
public interface C2gMessageMapper {

    /**
     * 保存C2C消息
     *
     * @param kafkaMessage 消息
     */
    @Insert("INSERT INTO c2g_msg(" +
            "message_id," +
            "sender_id," +
            "group_id," +
            "content," +
            "timestamp" +
            ") VALUES(" +
            "#{messageId}," +
            "#{senderId}," +
            "#{groupId}," +
            "#{content}," +
            "#{timestamp})")
    @Options(keyColumn = "message_id", keyProperty = "messageId", useGeneratedKeys = true)
    void saveMessage(KafkaMessage kafkaMessage);

}
