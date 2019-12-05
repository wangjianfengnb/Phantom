package com.phantom.business.mapper;

import com.phantom.business.domain.JoinGroupRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 会话成员Mapper
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 16:46
 */
@Mapper
public interface GroupMembersMapper {


    /**
     * 根据会话ID获取用户
     *
     * @param groupId 会话ID
     * @return 用户ID
     */
    @Select("SELECT user_account FROM group_members WHERE group_id = #{groupId}")
    List<String> getMembersByConversationId(@Param("groupId") Long groupId);


    /**
     * 插入会话关系
     *
     * @param groupId     会话ID
     * @param userAccount 用户ID
     */
    @Insert("INSERT INTO group_members(" +
            "group_id," +
            "user_account" +
            ") VALUES (" +
            "#{groupId}," +
            "#{userAccount})")
    void saveMembers(@Param("groupId") Long groupId, @Param("userAccount") String userAccount);

    /**
     * 获取群组关系
     */
    @Select("SELECT " +
            "relationship_id," +
            "group_id," +
            "user_account " +
            "FROM group_members " +
            "WHERE group_id = #{groupId} AND user_account = #{userAccount}")
    @Results({
            @Result(column = "relationship_id", property = "relationshipId"),
            @Result(column = "group_id", property = "groupId"),
            @Result(column = "user_account", property = "userAccount")
    })
    JoinGroupRequest getRelationship(Long groupId, String userAccount);
}
