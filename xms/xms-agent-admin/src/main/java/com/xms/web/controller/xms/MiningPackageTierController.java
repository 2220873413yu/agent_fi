package com.xms.web.controller.xms;

import java.util.Arrays;
import java.util.List;

import com.xms.common.annotation.RepeatSubmit;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.enums.BusinessType;
import com.xms.dao.domain.MiningPackageTier;
import com.xms.dao.service.IMiningPackageTierService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 矿机质押区间配置Controller
 *
 * @author xms
 * @date 2026-02-23
 */
@RestController
@RequestMapping("/xms/miningPackageTier")
public class MiningPackageTierController extends BaseController
{
    @Autowired
    private IMiningPackageTierService miningPackageTierService;

/**
 * 查询矿机质押区间配置列表
 */
@PreAuthorize("@ss.hasPermi('xms:miningPackageTier:list')")
@GetMapping("/list")
    public TableDataInfo list(MiningPackageTier miningPackageTier)
    {
        startPage();
        List<MiningPackageTier> list = miningPackageTierService.selectMiningPackageTierList(miningPackageTier);
        return getDataTable(list);
    }

    /**
     * 导出矿机质押区间配置列表
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageTier:export')")
    @Log(title = "矿机质押区间配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MiningPackageTier miningPackageTier)
    {
        List<MiningPackageTier> list = miningPackageTierService.selectMiningPackageTierList(miningPackageTier);
        ExcelUtil<MiningPackageTier> util = new ExcelUtil<MiningPackageTier>(MiningPackageTier.class);
        util.exportExcel(response, list, "矿机质押区间配置数据");
    }

    /**
     * 获取矿机质押区间配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageTier:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(miningPackageTierService.getById(id));
    }

    /**
     * 新增矿机质押区间配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageTier:add')")
    @Log(title = "矿机质押区间配置", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody MiningPackageTier miningPackageTier) {
        return toAjax(miningPackageTierService.saveMiningPackageTier(miningPackageTier));
    }

    /**
     * 修改矿机质押区间配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageTier:edit')")
    @Log(title = "矿机质押区间配置", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody MiningPackageTier miningPackageTier) {
        return toAjax(miningPackageTierService.updateMiningPackageTier(miningPackageTier));
    }

    /**
     * 删除矿机质押区间配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningPackageTier:remove')")
    @Log(title = "矿机质押区间配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(miningPackageTierService.removeRecordById(Arrays.asList(ids)));
    }
}
