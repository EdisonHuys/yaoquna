package com.ruoyi.web.controller.fatewheel;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.fatewheel.service.FwStatService;

/**
 * 数据统计（管理端）
 */
@RestController
@RequestMapping("/system/fwstat")
public class FwStatController extends BaseController
{
    @Autowired
    private FwStatService statService;

    @PreAuthorize("@ss.hasPermi('fatewheel:stat:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        Map<String, Object> data = statService.globalStats();
        return success(data);
    }
}
