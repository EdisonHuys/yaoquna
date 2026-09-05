package com.ruoyi.fatewheel.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import com.ruoyi.fatewheel.domain.FwUserExtend;

/**
 * 用户扩展 Mapper
 */
public interface FwUserExtendMapper
{
    @Select("select * from fw_user_extend where user_id = #{userId}")
    public FwUserExtend selectByUserId(Long userId);

    @Select("select * from fw_user_extend where openid = #{openid}")
    public FwUserExtend selectByOpenid(String openid);

    @Select("select * from fw_user_extend where invite_code = #{inviteCode}")
    public FwUserExtend selectByInvite(String inviteCode);

    @Options(useGeneratedKeys = true, keyProperty = "extendId")
    @Insert("insert into fw_user_extend(user_id, openid, streak, is_pro, shared_count, total_favorites, create_time, update_time) "
        + "values(#{userId}, #{openid}, 0, '0', 0, 0, sysdate(), sysdate())")
    public int insertFwUserExtend(FwUserExtend extend);

    @Update("<script>update fw_user_extend "
        + "<set>"
        + "<if test='schoolId != null'>school_id = #{schoolId},</if>"
        + "<if test='streak != null'>streak = #{streak},</if>"
        + "<if test='isPro != null'>is_pro = #{isPro},</if>"
        + "<if test='proExpireTime != null'>pro_expire_time = #{proExpireTime},</if>"
        + "<if test='sharedCount != null'>shared_count = #{sharedCount},</if>"
        + "<if test='totalFavorites != null'>total_favorites = #{totalFavorites},</if>"
        + "<if test='inviteCode != null'>invite_code = #{inviteCode},</if>"
        + "update_time = sysdate()"
        + "</set>"
        + "where user_id = #{userId}"
        + "</script>")
    public int updateFwUserExtend(FwUserExtend extend);

    /** 管理端：用户扩展列表（含 sys_user 基础信息） */
    @Select("<script>"
        + "select e.*, u.nick_name as nickName, u.avatar as avatar, u.phonenumber as phonenumber, "
        + "       s.school_name as schoolName "
        + "from fw_user_extend e "
        + "left join sys_user u on u.user_id = e.user_id "
        + "left join fw_school s on s.school_id = e.school_id "
        + "where 1=1 "
        + "<if test='userId != null'> and e.user_id = #{userId} </if>"
        + "<if test='isPro != null and isPro != \"\"'> and e.is_pro = #{isPro} </if>"
        + "order by e.create_time desc"
        + "</script>")
    public List<Map<String, Object>> selectFwUserExtendList(FwUserExtend query);
}
