package com.ruoyi.web.controller.fatewheel;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.fatewheel.domain.FwReport;
import com.ruoyi.fatewheel.service.FwReportService;

/**
 * 举报处理（管理端）
 */
@RestController
@RequestMapping("/system/fwreport")
public class FwReportController extends BaseController
{
    @Autowired
    private FwReportService reportService;

    @PreAuthorize("@ss.hasPermi('fatewheel:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(FwReport report)
    {
        startPage();
        List<Map<String, Object>> list = reportService.selectReportList(report);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:report:edit')")
    @Log(title = "举报处理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FwReport report)
    {
        return toAjax(reportService.handleReport(report));
    }
}
