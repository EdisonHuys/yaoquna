package com.ruoyi.fatewheel.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.fatewheel.domain.FwSchool;
import com.ruoyi.fatewheel.mapper.FwSchoolMapper;

/**
 * 学校业务
 */
@Service
public class FwSchoolService
{
    @Autowired
    private FwSchoolMapper schoolMapper;

    public List<FwSchool> selectSchoolList(FwSchool query)
    {
        return schoolMapper.selectFwSchoolList(query);
    }

    public FwSchool selectSchoolById(Long schoolId)
    {
        return schoolMapper.selectFwSchoolById(schoolId);
    }

    public int insertSchool(FwSchool school)
    {
        if (school.getStatus() == null)
        {
            school.setStatus("0");
        }
        return schoolMapper.insertFwSchool(school);
    }

    public int updateSchool(FwSchool school)
    {
        return schoolMapper.updateFwSchool(school);
    }

    public int deleteSchoolById(Long schoolId)
    {
        return schoolMapper.deleteFwSchoolById(schoolId);
    }
}
