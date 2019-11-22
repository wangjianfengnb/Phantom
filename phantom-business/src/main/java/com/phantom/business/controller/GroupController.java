package com.phantom.business.controller;

import com.phantom.business.mapper.ConversationMapper;
import com.phantom.business.mapper.ConversationMembersMapper;
import com.phantom.business.model.CreateGroupVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    private ConversationMapper conversationMapper;

    @Resource
    private ConversationMembersMapper conversationMembersMapper;

    /**
     * 创建会话
     *
     * @return 结果
     */
    @PostMapping("/create")
    public Boolean createGroup(@RequestBody CreateGroupVO createGroupVO) {
        conversationMapper.saveConversation(createGroupVO);
        Long conversationId = createGroupVO.getConversationId();
        for (Long uid : createGroupVO.getMembers()) {
            conversationMembersMapper.saveMembers(conversationId, uid);
        }
        return true;
    }

}
