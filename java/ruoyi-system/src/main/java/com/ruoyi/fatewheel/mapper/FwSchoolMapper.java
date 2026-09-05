package com.ruoyi.fatewheel.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Options;
import com.ruoyi.fatewheel.domain.FwSchool;

/**
 * 学校 Mapper
 */
public interface FwSchoolMapper
{
    @Select("select * from fw_school where school_id = #{schoolId}")
    public FwSchool selectFwSchoolById(Long schoolId);

    @Select("<script>"
        + "select * from fw_school "
        + "where 1=1 "
        + "<if test='schoolName != null and schoolName != \"\"'> and school_name like concat('%', #{schoolName}, '%') </if>"
        + "<if test='status != null and status != \"\"'> and status = #{status} </if>"
        + "order by sort asc"
        + "</script>")
    public List<FwSchool> selectFwSchoolList(FwSchool query);

    @Options(useGeneratedKeys = true, keyProperty = "schoolId")
    @Insert("insert into fw_school(school_name, school_code, city, sort, status, create_time, update_time) "
        + "values(#{schoolName}, #{schoolCode}, #{city}, #{sort}, #{status}, sysdate(), sysdate())")
    public int insertFwSchool(FwSchool school);

    @Update("update fw_school set school_name = #{schoolName}, school_code = #{schoolCode}, city = #{city}, "
        + "sort = #{sort}, status = #{status}, update_time = sysdate() where school_id = #{schoolId}")
    public int updateFwSchool(FwSchool school);

    @Update("delete from fw_school where school_id = #{schoolId}")
    public int deleteFwSchoolById(Long schoolId);
}
