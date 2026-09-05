package com.ruoyi.fatewheel.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fatewheel.domain.FwReport;
import com.ruoyi.fatewheel.mapper.FwReportMapper;

/**
 * 举报业务
 */
@Service
public class FwReportService
{
    @Autowired
    private FwReportMapper reportMapper;

    /** App 端：提交举报 */
    public int submitReport(FwReport report, Long userId)
    {
        report.setUserId(userId);
        if (report.getReason() == null || report.getReason().isEmpty())
        {
            report.setReason("内容不当");
        }
        return reportMapper.insertFwReport(report);
    }

    /** 管理端：举报列表（分页） */
    public List<Map<String, Object>> selectReportList(FwReport query)
    {
        return reportMapper.selectFwReportList(query);
    }

    /** 管理端：处理举报 */
    public int handleReport(FwReport report)
    {
        report.setHandleBy(SecurityUtils.getUserId());
        return reportMapper.handleFwReport(report);
    }
}
