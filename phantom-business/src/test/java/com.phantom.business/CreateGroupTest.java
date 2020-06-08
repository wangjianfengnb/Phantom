package com.phantom.business;

import com.phantom.business.domain.CreateUserRequest;
import com.phantom.business.mapper.GroupMapper;
import com.phantom.business.mapper.GroupMembersMapper;
import com.phantom.business.mapper.UserMapper;
import com.phantom.business.model.CreateGroupVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjianfeng
 * @since 2020/6/8
 */
@RunWith(SpringRunner.class)
@SpringBootTest
//@Transactional(rollbackFor = Exception.class)
public class CreateGroupTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GroupMapper groupMapper;

    @Autowired
    private GroupMembersMapper groupMembersMapper;

    @Test
    public void testCreateUser() {
        int userCount = 3;
        List<String> ids = new ArrayList<>(userCount);
        for (int i = 0; i < userCount; i++) {
            String id = "test" + i;
            ids.add(id);
            userMapper.saveUser(CreateUserRequest.builder()
                    .avatar("vv")
                    .userName(id)
                    .userPassword("xxx")
                    .userAccount(id)
                    .build());
        }
        CreateGroupVO vo = CreateGroupVO.builder()
                .groupAvatar("xxxx")
                .groupName("测试群聊")
                .build();
        groupMapper.saveGroupInfo(vo);
        Long conversationId = vo.getGroupId();
        for (String member : ids) {
            groupMembersMapper.saveMembers(conversationId, member);
        }

    }


}
