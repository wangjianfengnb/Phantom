package com.phantom.business.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 加入群组
 *
 * @author Jianfeng Wang
 * @since 2019/12/5 15:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JoinGroupRequest {
    /**
     * 用户ID
     */
    private String userAccount;
    /**
     * 群组ID
     */
    private Long groupId;

    /**
     * 关系ID
     */
    private Long relationshipId;
}
