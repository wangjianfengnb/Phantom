package com.phantom.business.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 群组信息
 *
 * @author Jianfeng Wang
 * @since 2019/12/5 15:32
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponse {

    /**
     * 群组ID
     */
    private Long groupId;
    /**
     * 群组名称
     */
    private String groupName;
    /**
     * 群组头像
     */
    private String groupAvatar;

    /**
     * 成员
     */
    private List<String> members;

}
