package com.phantom.business.controller;

import com.phantom.business.domain.GroupResponse;
import com.phantom.business.domain.JoinGroupRequest;
import com.phantom.business.mapper.GroupMapper;
import com.phantom.business.mapper.GroupMembersMapper;
import com.phantom.business.model.CreateGroupVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 群聊Controller
 *
 * @author Jianfeng Wang
 * @since 2019/11/19 16:32
 */
@RestController
@RequestMapping("/group")
public class GroupController {

    @Resource
    private GroupMapper groupMapper;

    @Resource
    private GroupMembersMapper groupMembersMapper;

    /**
     * 创建会话
     *
     * @return 结果
     */
    @PostMapping("/create")
    public Boolean createGroup(@RequestBody CreateGroupVO createGroupVO) {
        groupMapper.saveGroupInfo(createGroupVO);
        Long conversationId = createGroupVO.getGroupId();
        for (String member : createGroupVO.getMembers()) {
            groupMembersMapper.saveMembers(conversationId, member);
        }
        return true;
    }

    /**
     * 获取回话信息
     *
     * @param groupId 回话ID
     * @return 回话信息
     */
    @GetMapping("/{groupId}")
    public GroupResponse getGroupInfo(@PathVariable("groupId") Long groupId) {
        GroupResponse groupResponse = groupMapper.getById(groupId);
        List<String> members = groupMembersMapper.getMembersByConversationId(groupId);
        groupResponse.setMembers(members);
        return groupResponse;
    }

    /**
     * 加入群组
     */
    @PostMapping("/joinGroup")
    public Boolean joinGroup(@RequestBody JoinGroupRequest joinGroupRequest) {
        JoinGroupRequest relationship = groupMembersMapper.getRelationship(joinGroupRequest.getGroupId(),
                joinGroupRequest.getUserAccount());
        if (relationship == null) {
            groupMembersMapper.saveMembers(joinGroupRequest.getGroupId(), joinGroupRequest.getUserAccount());
        }
        return true;
    }


    /**
     * 分页获取群组
     *
     * @return 群组
     */
    @GetMapping("/list")
    public List<GroupResponse> listGroup(Integer page, Integer size) {
        List<GroupResponse> groupResponses = groupMapper.listByPage(size, size * page);
        if (groupResponses == null) {
            return new ArrayList<>();
        }
        return groupResponses;
    }


}
