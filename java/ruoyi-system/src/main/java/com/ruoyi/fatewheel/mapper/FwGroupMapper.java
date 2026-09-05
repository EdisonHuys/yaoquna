package com.ruoyi.fatewheel.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import com.ruoyi.fatewheel.domain.FwGroup;
import com.ruoyi.fatewheel.domain.FwGroupMember;

/**
 * 小组 Mapper
 */
public interface FwGroupMapper
{
    @Select("select * from fw_group where group_id = #{groupId} and status = '0'")
    public FwGroup selectFwGroupById(Long groupId);

    @Select("select * from fw_group where invite_code = #{inviteCode} and status = '0'")
    public FwGroup selectFwGroupByInvite(String inviteCode);

    @Options(useGeneratedKeys = true, keyProperty = "groupId")
    @Insert("insert into fw_group(group_name, owner_id, invite_code, member_count, status, create_time, update_time) "
        + "values(#{groupName}, #{ownerId}, #{inviteCode}, 1, '0', sysdate(), sysdate())")
    public int insertFwGroup(FwGroup group);

    @Update("update fw_group set member_count = member_count + 1, update_time = sysdate() where group_id = #{groupId}")
    public int incrMember(Long groupId);

    @Update("update fw_group set member_count = case when member_count > 1 then member_count - 1 else 1 end, update_time = sysdate() where group_id = #{groupId}")
    public int decrMember(Long groupId);

    /** 我加入的小组 */
    @Select("select g.* from fw_group g inner join fw_group_member gm on gm.group_id = g.group_id "
        + "where gm.user_id = #{userId} and g.status = '0' order by g.create_time desc")
    public List<FwGroup> selectMyGroups(@Param("userId") Long userId);

    /** 组成员列表（含昵称） */
    @Select("select gm.user_id as userId, gm.role as role, u.nick_name as nickName, u.avatar as avatar "
        + "from fw_group_member gm left join sys_user u on u.user_id = gm.user_id "
        + "where gm.group_id = #{groupId} order by gm.role asc, gm.join_time asc")
    public List<Map<String, Object>> selectGroupMembers(Long groupId);

    @Insert("insert into fw_group_member(group_id, user_id, role, join_time) values(#{groupId}, #{userId}, '1', sysdate())")
    public int insertMember(FwGroupMember member);

    @Select("select count(*) from fw_group_member where group_id = #{groupId} and user_id = #{userId}")
    public int countMember(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Select("select count(*) from fw_group_member where group_id = #{groupId}")
    public int countMembers(Long groupId);

    /** 管理端：全部小组列表 */
    @Select("select g.*, u.nick_name as ownerName from fw_group g left join sys_user u on u.user_id = g.owner_id "
        + "where g.status = '0' order by g.create_time desc")
    public List<Map<String, Object>> selectFwGroupList(FwGroup query);
}
