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
import com.xms.dao.domain.WithdrawalConfig;
import com.xms.dao.service.IWithdrawalConfigService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 提现配置Controller
 *
 * @author xms
 * @date 2026-02-06
 */
@RestController
@RequestMapping("/xms/withdrawalConfig")
public class WithdrawalConfigController extends BaseController
{
    @Autowired
    private IWithdrawalConfigService withdrawalConfigService;

/**
 * 查询提现配置列表
 */
@PreAuthorize("@ss.hasPermi('xms:withdrawalConfig:list')")
@GetMapping("/list")
    public TableDataInfo list(WithdrawalConfig withdrawalConfig)
    {
        startPage();
        List<WithdrawalConfig> list = withdrawalConfigService.selectWithdrawalConfigList(withdrawalConfig);
        return getDataTable(list);
    }

    /**
     * 导出提现配置列表
     */
    @PreAuthorize("@ss.hasPermi('xms:withdrawalConfig:export')")
    @Log(title = "提现配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WithdrawalConfig withdrawalConfig)
    {
        List<WithdrawalConfig> list = withdrawalConfigService.selectWithdrawalConfigList(withdrawalConfig);
        ExcelUtil<WithdrawalConfig> util = new ExcelUtil<WithdrawalConfig>(WithdrawalConfig.class);
        util.exportExcel(response, list, "提现配置数据");
    }

    /**
     * 获取提现配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:withdrawalConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(withdrawalConfigService.getById(id));
    }

    /**
     * 新增提现配置
     */
    @PreAuthorize("@ss.hasPermi('xms:withdrawalConfig:add')")
    @Log(title = "提现配置", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody WithdrawalConfig withdrawalConfig) {
        return toAjax(withdrawalConfigService.save(withdrawalConfig));
    }

    /**
     * 修改提现配置
     */
    @PreAuthorize("@ss.hasPermi('xms:withdrawalConfig:edit')")
    @Log(title = "提现配置", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody WithdrawalConfig withdrawalConfig) {
        return toAjax(withdrawalConfigService.updateByConfigById(withdrawalConfig));
    }

    /**
     * 删除提现配置
     */
    @PreAuthorize("@ss.hasPermi('xms:withdrawalConfig:remove')")
    @Log(title = "提现配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(withdrawalConfigService.removeByIds(Arrays.asList(ids)));
    }
}
