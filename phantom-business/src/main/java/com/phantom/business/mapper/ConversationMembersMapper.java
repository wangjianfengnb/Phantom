package com.phantom.business.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 会话成员Mapper
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 16:46
 */
@Mapper
public interface ConversationMembersMapper {


    /**
     * 根据会话ID获取用户
     *
     * @param conversationId 会话ID
     * @return 用户ID
     */
    @Select("SELECT user_id FROM conversation_members WHERE conversation_id = #{conversationId}")
    List<Long> getMembersByConversationId(@Param("conversationId") Long conversationId);


    /**
     * 插入会话关系
     *
     * @param conversationId 会话ID
     * @param userId         用户ID
     */
    @Insert("INSERT INTO conversation_members(" +
            "conversation_id," +
            "user_id" +
            ") VALUES (" +
            "#{conversationId}," +
            "#{userId})")
    void saveMembers(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
