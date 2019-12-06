package com.phantom.business.mapper;

import com.phantom.business.domain.GroupResponse;
import com.phantom.business.model.CreateGroupVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

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
    @Insert("INSERT INTO group_info(" +
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
            "FROM group_info WHERE group_id = #{groupId}")
    @Results({
            @Result(column = "group_id", property = "groupId"),
            @Result(column = "group_name", property = "groupName"),
            @Result(column = "group_avatar", property = "groupAvatar")
    })
    GroupResponse getById(@Param("groupId") Long groupId);


    /**
     * 获取群组
     */
    @Select("SELECT " +
            "group_id," +
            "group_name," +
            "group_avatar " +
            "FROM group_info limit #{offset},#{size}")
    @Results({
            @Result(column = "group_id", property = "groupId"),
            @Result(column = "group_name", property = "groupName"),
            @Result(column = "group_avatar", property = "groupAvatar")
    })
    List<GroupResponse> listByPage(@Param("size") Integer size, @Param("offset") Integer offset);

}
