package com.phantom.business.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建用户
 *
 * @author Jianfeng Wang
 * @since 2019/12/5 15:59
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserRequest {

    /**
     * 用户名
     */
    private String userName;
    /**
     * 用户密码
     */
    private String userPassword;
    /**
     * 用户账号
     */
    private String userAccount;
    /**
     * 用户头像
     */
    private String avatar;


}
