package com.ruoyi.fatewheel.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import com.ruoyi.fatewheel.domain.FwReport;

/**
 * 举报 Mapper
 */
public interface FwReportMapper
{
    @Select("select * from fw_report where report_id = #{reportId}")
    public FwReport selectFwReportById(Long reportId);

    @Select("<script>"
        + "select r.*, u.nick_name as reporterName, m.name as markName "
        + "from fw_report r "
        + "left join sys_user u on u.user_id = r.user_id "
        + "left join fw_mark m on m.mark_id = r.mark_id "
        + "where 1=1 "
        + "<if test='status != null and status != \"\"'> and r.status = #{status} </if>"
        + "<if test='markId != null'> and r.mark_id = #{markId} </if>"
        + "order by r.create_time desc"
        + "</script>")
    public List<java.util.Map<String, Object>> selectFwReportList(FwReport query);

    @Options(useGeneratedKeys = true, keyProperty = "reportId")
    @Insert("insert into fw_report(user_id, mark_id, reason, status, create_time) "
        + "values(#{userId}, #{markId}, #{reason}, 'pending', sysdate())")
    public int insertFwReport(FwReport report);

    @Update("update fw_report set status = #{status}, handle_by = #{handleBy}, handle_time = sysdate(), handle_remark = #{handleRemark} "
        + "where report_id = #{reportId}")
    public int handleFwReport(FwReport report);

    @Select("select count(*) from fw_report where mark_id = #{markId} and status = 'pending'")
    public int countPendingByMark(@Param("markId") Long markId);
}
