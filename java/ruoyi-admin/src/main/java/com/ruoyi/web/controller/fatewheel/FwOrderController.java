package com.ruoyi.web.controller.fatewheel;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.fatewheel.domain.FwOrder;
import com.ruoyi.fatewheel.service.FwOrderService;

/**
 * 订单管理（管理端）
 */
@RestController
@RequestMapping("/system/fworder")
public class FwOrderController extends BaseController
{
    @Autowired
    private FwOrderService orderService;

    @PreAuthorize("@ss.hasPermi('fatewheel:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(FwOrder query)
    {
        startPage();
        List<FwOrder> list = orderService.selectOrderList(query);
        return getDataTable(list);
    }
}
