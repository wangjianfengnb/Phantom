package com.phantom.business.mapper;

import com.phantom.business.model.CreateGroupVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 会话Mapper组件
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 16:43
 */
@Mapper
public interface ConversationMapper {

    /**
     * 保存回话
     *
     * @param createGroupVO 创建会话
     */
    @Insert("INSERT INTO conversation(" +
            "conversation_name," +
            "conversation_avatar" +
            ") VALUES (" +
            "#{conversationName}," +
            "#{conversationAvatar})")
    @Options(keyProperty = "conversationId", keyColumn = "conversation_id", useGeneratedKeys = true)
    void saveConversation(CreateGroupVO createGroupVO);

}
