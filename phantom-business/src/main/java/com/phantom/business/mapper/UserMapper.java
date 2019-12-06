package com.phantom.business.mapper;

import com.phantom.business.domain.CreateUserRequest;
import com.phantom.business.domain.UserResponse;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户Mapper组件
 *
 * @author Jianfeng Wang
 * @since 2019/12/5 16:02
 */
@Mapper
public interface UserMapper {

    /**
     * 保存用户
     *
     * @param request 用户请求
     */
    @Insert("INSERT INTO user(" +
            "user_name," +
            "user_account," +
            "user_password," +
            "avatar) " +
            "VALUES(" +
            "#{userName}," +
            "#{userAccount}," +
            "#{userPassword}," +
            "#{avatar})")
    void saveUser(CreateUserRequest request);


    /**
     * 获取用户
     *
     * @param userAccount 用户名
     */
    @Select("SELECT " +
            "user_id," +
            "user_name," +
            "user_password," +
            "user_account," +
            "avatar " +
            "FROM user WHERE user_account = #{userAccount}")
    @Results({
            @Result(column = "user_id", property = "userId"),
            @Result(column = "user_name", property = "userName"),
            @Result(column = "user_password", property = "userPassword"),
            @Result(column = "user_account", property = "userAccount"),
            @Result(column = "avatar", property = "avatar")
    })
    UserResponse getUser(@Param("userAccount") String userAccount);


    /**
     * 获取用户
     *
     * @param userAccount 用户名
     */
    @Select("SELECT " +
            "user_id," +
            "user_name," +
            "user_password," +
            "user_account," +
            "avatar " +
            "FROM user WHERE user_account != #{userAccount} ORDER BY user_id DESC LIMIT #{offset},#{size}")
    @Results({
            @Result(column = "user_id", property = "userId"),
            @Result(column = "user_name", property = "userName"),
            @Result(column = "user_password", property = "userPassword"),
            @Result(column = "user_account", property = "userAccount"),
            @Result(column = "avatar", property = "avatar")
    })
    List<UserResponse> listByPage(@Param("userAccount") String userAccount,@Param("size") Integer size,@Param("offset") Integer offset);
}
