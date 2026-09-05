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
import com.ruoyi.fatewheel.domain.FwSchool;
import com.ruoyi.fatewheel.service.FwSchoolService;

/**
 * 学校管理（管理端）
 */
@RestController
@RequestMapping("/system/fwschool")
public class FwSchoolController extends BaseController
{
    @Autowired
    private FwSchoolService schoolService;

    @PreAuthorize("@ss.hasPermi('fatewheel:school:list')")
    @GetMapping("/list")
    public TableDataInfo list(FwSchool school)
    {
        startPage();
        List<FwSchool> list = schoolService.selectSchoolList(school);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:school:query')")
    @GetMapping(value = "/{schoolId}")
    public AjaxResult getInfo(@PathVariable Long schoolId)
    {
        return success(schoolService.selectSchoolById(schoolId));
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:school:add')")
    @Log(title = "学校管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FwSchool school)
    {
        return toAjax(schoolService.insertSchool(school));
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:school:edit')")
    @Log(title = "学校管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FwSchool school)
    {
        return toAjax(schoolService.updateSchool(school));
    }

    @PreAuthorize("@ss.hasPermi('fatewheel:school:remove')")
    @Log(title = "学校管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{schoolIds}")
    public AjaxResult remove(@PathVariable Long[] schoolIds)
    {
        for (Long id : schoolIds)
        {
            schoolService.deleteSchoolById(id);
        }
        return success();
    }
}
