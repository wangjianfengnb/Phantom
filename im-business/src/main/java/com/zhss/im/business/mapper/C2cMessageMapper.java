package com.zhss.im.business.mapper;

import com.zhss.im.common.model.C2cMessage;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 单聊消息mapper
 *
 * @author Jianfeng Wang
 * @since 2019/11/12 17:39
 */
@Mapper
public interface C2cMessageMapper {


    /**
     * 保存C2C消息
     *
     * @param c2CMessage 消息
     */
    @Insert("INSERT INTO c2c_msg(" +
            "message_id," +
            "sender_id," +
            "receiver_id," +
            "content," +
            "timestamp" +
            ") VALUES(" +
            "#{messageId}," +
            "#{senderId}," +
            "#{receiverId}," +
            "#{content}," +
            "#{timestamp})")
    @Options(keyColumn = "message_id", keyProperty = "messageId", useGeneratedKeys = true)
    void saveMessage(C2cMessage c2CMessage);


}
