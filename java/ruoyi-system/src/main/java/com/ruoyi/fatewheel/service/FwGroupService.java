package com.ruoyi.fatewheel.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.fatewheel.domain.FwGroup;
import com.ruoyi.fatewheel.domain.FwGroupMember;
import com.ruoyi.fatewheel.mapper.FwGroupMapper;

/**
 * 小组业务（≤5 人邀请制）
 */
@Service
public class FwGroupService
{
    @Autowired
    private FwGroupMapper groupMapper;

    /** 我的小组 */
    public List<FwGroup> selectMyGroups(Long userId)
    {
        return groupMapper.selectMyGroups(userId);
    }

    /** 创建小组 */
    public FwGroup createGroup(Long userId, String groupName)
    {
        if (StringUtils.isEmpty(groupName))
        {
            throw new ServiceException("小组名称不能为空");
        }
        FwGroup group = new FwGroup();
        group.setGroupName(groupName);
        group.setOwnerId(userId);
        group.setInviteCode(genInviteCode());
        group.setMemberCount(1);
        group.setStatus("0");
        groupMapper.insertFwGroup(group);
        // 组长作为成员加入
        FwGroupMember owner = new FwGroupMember();
        owner.setGroupId(group.getGroupId());
        owner.setUserId(userId);
        owner.setRole("0");
        groupMapper.insertMember(owner);
        return group;
    }

    /** 通过邀请码加入小组（上限 5 人） */
    public FwGroup joinGroup(Long userId, String inviteCode)
    {
        FwGroup group = groupMapper.selectFwGroupByInvite(inviteCode);
        if (group == null)
        {
            throw new ServiceException("邀请码无效");
        }
        if (groupMapper.countMember(group.getGroupId(), userId) > 0)
        {
            throw new ServiceException("你已在该小组中");
        }
        int memberCount = groupMapper.countMembers(group.getGroupId());
        if (memberCount >= 5)
        {
            throw new ServiceException("小组人数已满（最多5人）");
        }
        groupMapper.insertMember(new FwGroupMember()
        {
            {
                setGroupId(group.getGroupId());
                setUserId(userId);
                setRole("1");
            }
        });
        groupMapper.incrMember(group.getGroupId());
        group.setMemberCount(memberCount + 1);
        return group;
    }

    /** 组成员列表（含昵称） */
    public List<Map<String, Object>> selectMembers(Long groupId)
    {
        return groupMapper.selectGroupMembers(groupId);
    }

    /** 管理端：小组列表 */
    public List<Map<String, Object>> selectGroupList(FwGroup query)
    {
        return groupMapper.selectFwGroupList(query);
    }

    private String genInviteCode()
    {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++)
        {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}
