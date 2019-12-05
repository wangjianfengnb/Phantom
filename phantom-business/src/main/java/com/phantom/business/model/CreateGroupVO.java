package com.phantom.business.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建群聊
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 16:41
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CreateGroupVO {

    /**
     * 会话名称
     */
    private String groupName;
    /**
     * 会话头像
     */
    private String groupAvatar;
    /**
     * 会话成员
     */
    private List<String> members;

    /**
     * 会话ID
     */
    private Long groupId;


}