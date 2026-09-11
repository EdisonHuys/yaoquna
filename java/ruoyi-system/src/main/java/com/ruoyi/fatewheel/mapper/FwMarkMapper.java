package com.ruoyi.fatewheel.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import com.ruoyi.fatewheel.domain.FwMark;
import com.ruoyi.fatewheel.domain.vo.FwMarkVO;

/**
 * 地点标记 Mapper
 */
public interface FwMarkMapper
{
    @Select("select * from fw_mark where mark_id = #{markId} and del_flag = '0'")
    public FwMark selectFwMarkById(Long markId);

    /**
     * 条件查询（管理端 + App 通用）
     * 支持：创建者、分类、共享范围、学校、状态、关键字
     */
    @Select("<script>"
        + "select m.*, u.nick_name as creatorName, u.avatar as creatorAvatar, "
        + "       s.school_name as schoolName "
        + "from fw_mark m "
        + "left join sys_user u on u.user_id = m.user_id "
        + "left join fw_school s on s.school_id = m.school_id "
        + "where m.del_flag = '0' "
        + "<if test='markId != null'> and m.mark_id = #{markId} </if>"
        + "<if test='userId != null'> and m.user_id = #{userId} </if>"
        + "<if test='category != null and category != \"\" and category != \"全部\"'> and m.category = #{category} </if>"
        + "<if test='shareScope != null and shareScope != \"\"'> and m.share_scope = #{shareScope} </if>"
        + "<if test='schoolId != null'> and m.school_id = #{schoolId} </if>"
        + "<if test='status != null and status != \"\"'> and m.status = #{status} </if>"
        + "<if test='keyword != null and keyword != \"\"'> and (m.name like concat('%', #{keyword}, '%') or m.address like concat('%', #{keyword}, '%')) </if>"
        + "order by m.create_time desc"
        + "</script>")
    public List<FwMarkVO> selectFwMarkList(FwMark query);

    @Options(useGeneratedKeys = true, keyProperty = "markId")
    @Insert("insert into fw_mark(user_id, name, category, lat, lng, address, remark, rating, price, tags, images, "
        + "share_scope, group_ids, school_id, status, del_flag, favorite_count, create_time, update_time) "
        + "values(#{userId}, #{name}, #{category}, #{lat}, #{lng}, #{address}, #{remark}, #{rating}, #{price}, #{tags}, #{images}, "
        + "#{shareScope}, #{groupIds}, #{schoolId}, #{status}, '0', 0, sysdate(), sysdate())")
    public int insertFwMark(FwMark mark);

    @Update("<script>update fw_mark "
        + "<set>"
        + "<if test='name != null'>name = #{name},</if>"
        + "<if test='category != null'>category = #{category},</if>"
        + "<if test='lat != null'>lat = #{lat},</if>"
        + "<if test='lng != null'>lng = #{lng},</if>"
        + "<if test='address != null'>address = #{address},</if>"
        + "<if test='remark != null'>remark = #{remark},</if>"
        + "<if test='rating != null'>rating = #{rating},</if>"
        + "<if test='price != null'>price = #{price},</if>"
        + "<if test='tags != null'>tags = #{tags},</if>"
        + "<if test='images != null'>images = #{images},</if>"
        + "<if test='shareScope != null'>share_scope = #{shareScope},</if>"
        + "<if test='groupIds != null'>group_ids = #{groupIds},</if>"
        + "<if test='schoolId != null'>school_id = #{schoolId},</if>"
        + "<if test='status != null'>status = #{status},</if>"
        + "update_time = sysdate()"
        + "</set>"
        + "where mark_id = #{markId}"
        + "</script>")
    public int updateFwMark(FwMark mark);

    @Update("update fw_mark set del_flag = '2', update_time = sysdate() where mark_id = #{markId}")
    public int deleteFwMarkById(Long markId);

    /** 收藏数 +1 */
    @Update("update fw_mark set favorite_count = favorite_count + 1 where mark_id = #{markId}")
    public int incrFavorite(Long markId);

    /** 收藏数 -1（不低于0） */
    @Update("update fw_mark set favorite_count = case when favorite_count > 0 then favorite_count - 1 else 0 end where mark_id = #{markId}")
    public int decrFavorite(Long markId);

    /** 统计某个用户已共享的地点数 */
    @Select("select count(*) from fw_mark where user_id = #{userId} and share_scope != 'private' and del_flag = '0'")
    public int countShared(@Param("userId") Long userId);

    /** 统计标记总数（管理端） */
    @Select("select count(*) from fw_mark where del_flag = '0'")
    public long countAll();

    /** 按分类统计数量 */
    @Select("select category, count(*) as cnt from fw_mark where del_flag = '0' group by category")
    public List<java.util.Map<String, Object>> countByCategory();
}
