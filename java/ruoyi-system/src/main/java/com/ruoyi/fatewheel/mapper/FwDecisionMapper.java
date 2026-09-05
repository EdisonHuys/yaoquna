package com.ruoyi.fatewheel.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import com.ruoyi.fatewheel.domain.FwDecision;

/**
 * 决策记录 Mapper
 */
public interface FwDecisionMapper
{
    @Select("select * from fw_decision where decision_id = #{decisionId}")
    public FwDecision selectFwDecisionById(Long decisionId);

    @Select("<script>"
        + "select * from fw_decision "
        + "where 1=1 "
        + "<if test='userId != null'> and user_id = #{userId} </if>"
        + "<if test='decisionType != null and decisionType != \"\"'> and decision_type = #{decisionType} </if>"
        + "order by create_time desc"
        + "</script>")
    public List<FwDecision> selectFwDecisionList(FwDecision query);

    @Options(useGeneratedKeys = true, keyProperty = "decisionId")
    @Insert("insert into fw_decision(user_id, decision_type, title, result, detail, executed, create_time) "
        + "values(#{userId}, #{decisionType}, #{title}, #{result}, #{detail}, #{executed}, sysdate())")
    public int insertFwDecision(FwDecision decision);

    @Select("select count(*) from fw_decision where user_id = #{userId}")
    public long countByUser(@Param("userId") Long userId);

    /** 本月决策数 */
    @Select("select count(*) from fw_decision where user_id = #{userId} and create_time >= date_format(sysdate(), '%Y-%m-01')")
    public long countByUserThisMonth(@Param("userId") Long userId);

    /** 按类型统计用户决策次数 */
    @Select("select decision_type, count(*) as cnt from fw_decision where user_id = #{userId} group by decision_type")
    public List<java.util.Map<String, Object>> countByType(@Param("userId") Long userId);
}
