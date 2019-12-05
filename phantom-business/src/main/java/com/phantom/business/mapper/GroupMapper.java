package com.phantom.business.mapper;

import com.phantom.business.domain.GroupResponse;
import com.phantom.business.model.CreateGroupVO;
import org.apache.ibatis.annotations.*;

/**
 * 会话Mapper组件
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 16:43
 */
@Mapper
public interface GroupMapper {

    /**
     * 保存群组
     */
    @Insert("INSERT INTO group(" +
            "group_name," +
            "group_avatar" +
            ") VALUES (" +
            "#{groupName}," +
            "#{groupAvatar})")
    @Options(keyProperty = "groupId", keyColumn = "group_id", useGeneratedKeys = true)
    void saveConversation(CreateGroupVO createGroupVO);

    /**
     * 获取群组
     */
    @Select("SELECT " +
            "group_id," +
            "group_name," +
            "group_avatar " +
            "FROM group WHERE group_id = #{groupId}")
    @Results({
            @Result(column = "group_id", property = "groupId"),
            @Result(column = "group_name", property = "groupName"),
            @Result(column = "group_avatar", property = "groupAvatar")
    })
    GroupResponse getById(@Param("groupId") Long groupId);
}
