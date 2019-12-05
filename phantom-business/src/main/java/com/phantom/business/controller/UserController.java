package com.phantom.business.controller;

import com.phantom.business.domain.CreateUserRequest;
import com.phantom.business.domain.UserResponse;
import com.phantom.business.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户管理
 *
 * @author Jianfeng Wang
 * @since 2019/12/5 15:59
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserMapper userMapper;

    /**
     * 创建用户
     */
    @PostMapping("/")
    public Boolean createUser(@RequestBody CreateUserRequest request) {
        try {
            userMapper.saveUser(request);
            return true;
        } catch (Exception e) {
            log.info("创建用户错误");
            return false;
        }
    }

    /**
     * 获取用户
     */
    @GetMapping("/{userAccount}")
    public UserResponse getUser(@PathVariable("userAccount") String userAccount) {
        UserResponse user = userMapper.getUser(userAccount);
        if (user == null) {
            return new UserResponse();
        }
        return user;
    }

}
