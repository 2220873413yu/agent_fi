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
import com.xms.dao.domain.MiningRewardConfig;
import com.xms.dao.service.IMiningRewardConfigService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 矿机奖励分配配置Controller
 *
 * @author xms
 * @date 2026-02-25
 */
@RestController
@RequestMapping("/xms/miningRewardConfig")
public class MiningRewardConfigController extends BaseController
{
    @Autowired
    private IMiningRewardConfigService miningRewardConfigService;

/**
 * 查询矿机奖励分配配置列表
 */
@PreAuthorize("@ss.hasPermi('xms:miningRewardConfig:list')")
@GetMapping("/list")
    public TableDataInfo list(MiningRewardConfig miningRewardConfig)
    {
        startPage();
        List<MiningRewardConfig> list = miningRewardConfigService.selectMiningRewardConfigList(miningRewardConfig);
        return getDataTable(list);
    }

    /**
     * 导出矿机奖励分配配置列表
     */
    @PreAuthorize("@ss.hasPermi('xms:miningRewardConfig:export')")
    @Log(title = "矿机奖励分配配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, MiningRewardConfig miningRewardConfig)
    {
        List<MiningRewardConfig> list = miningRewardConfigService.selectMiningRewardConfigList(miningRewardConfig);
        ExcelUtil<MiningRewardConfig> util = new ExcelUtil<MiningRewardConfig>(MiningRewardConfig.class);
        util.exportExcel(response, list, "矿机奖励分配配置数据");
    }

    /**
     * 获取矿机奖励分配配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:miningRewardConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(miningRewardConfigService.getById(id));
    }

    /**
     * 新增矿机奖励分配配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningRewardConfig:add')")
    @Log(title = "矿机奖励分配配置", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody MiningRewardConfig miningRewardConfig) {
        return toAjax(miningRewardConfigService.save(miningRewardConfig));
    }

    /**
     * 修改矿机奖励分配配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningRewardConfig:edit')")
    @Log(title = "矿机奖励分配配置", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody MiningRewardConfig miningRewardConfig) {
		miningRewardConfig.setUpdateBy(getUsername());
        return toAjax(miningRewardConfigService.updateConfigById(miningRewardConfig));
    }

    /**
     * 删除矿机奖励分配配置
     */
    @PreAuthorize("@ss.hasPermi('xms:miningRewardConfig:remove')")
    @Log(title = "矿机奖励分配配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(miningRewardConfigService.removeByIds(Arrays.asList(ids)));
    }
}
