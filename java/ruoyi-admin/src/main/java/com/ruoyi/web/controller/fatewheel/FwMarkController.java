package com.ruoyi.web.controller.fatewheel;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.fatewheel.domain.FwMark;
import com.ruoyi.fatewheel.domain.vo.FwMarkVO;
import com.ruoyi.fatewheel.service.FwMarkService;

/**
 * 标记管理（管理端）
 */
@RestController
@RequestMapping("/system/fwmark")
public class FwMarkController extends BaseController
{
    @Autowired
    private FwMarkService markService;

    @PreAuthorize("@ss.hasPermi('fatewheel:mark:list')")
    @GetMapping("/list")
    public TableDataInfo list(FwMark mark)
    {
        startPage();
        List<FwMarkVO> list = markService.selectFwMarkList(mark);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:mark:query')")
    @GetMapping(value = "/{markId}")
    public AjaxResult getInfo(@PathVariable Long markId)
    {
        return success(markService.selectFwMarkById(markId));
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:mark:add')")
    @Log(title = "地点标记", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FwMark mark)
    {
        return success(markService.insertFwMark(mark, getUserId()));
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:mark:edit')")
    @Log(title = "地点标记", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FwMark mark)
    {
        markService.updateFwMark(mark);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:mark:remove')")
    @Log(title = "地点标记", businessType = BusinessType.DELETE)
    @DeleteMapping("/{markIds}")
    public AjaxResult remove(@PathVariable Long[] markIds)
    {
        for (Long id : markIds)
        {
            markService.deleteFwMarkById(id);
        }
        return success();
    }
}
