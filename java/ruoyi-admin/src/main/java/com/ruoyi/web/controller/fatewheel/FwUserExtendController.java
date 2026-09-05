package com.ruoyi.web.controller.fatewheel;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.fatewheel.domain.FwUserExtend;
import com.ruoyi.fatewheel.service.FwUserService;

/**
 * 用户扩展管理（管理端）：查看 App 用户信息、学校、Pro 状态等
 */
@RestController
@RequestMapping("/system/fwuserext")
public class FwUserExtendController extends BaseController
{
    @Autowired
    private FwUserService userService;

    @PreAuthorize("@ss.hasPermi('fatewheel:userext:list')")
    @GetMapping("/list")
    public TableDataInfo list(FwUserExtend query)
    {
        startPage();
        List<Map<String, Object>> list = userService.selectUserExtendList(query);
        return getDataTable(list);
    }
}
