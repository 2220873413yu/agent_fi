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
import com.xms.dao.domain.UserLevelConfig;
import com.xms.dao.service.IUserLevelConfigService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 用户等级考核配置Controller
 *
 * @author xms
 * @date 2025-12-03
 */
@RestController
@RequestMapping("/xms/userLevelConfig")
public class UserLevelConfigController extends BaseController
{
    @Autowired
    private IUserLevelConfigService userLevelConfigService;

/**
 * 查询用户等级考核配置列表
 */
@PreAuthorize("@ss.hasPermi('xms:userLevelConfig:list')")
@GetMapping("/list")
    public TableDataInfo list(UserLevelConfig userLevelConfig)
    {
        startPage();
        List<UserLevelConfig> list = userLevelConfigService.selectUserLevelConfigList(userLevelConfig);
        return getDataTable(list);
    }

    /**
     * 导出用户等级考核配置列表
     */
    @PreAuthorize("@ss.hasPermi('xms:userLevelConfig:export')")
    @Log(title = "用户等级考核配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserLevelConfig userLevelConfig)
    {
        List<UserLevelConfig> list = userLevelConfigService.selectUserLevelConfigList(userLevelConfig);
        ExcelUtil<UserLevelConfig> util = new ExcelUtil<UserLevelConfig>(UserLevelConfig.class);
        util.exportExcel(response, list, "用户等级考核配置数据");
    }

    /**
     * 获取用户等级考核配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:userLevelConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(userLevelConfigService.getById(id));
    }

    /**
     * 新增用户等级考核配置
     */
    @PreAuthorize("@ss.hasPermi('xms:userLevelConfig:add')")
    @Log(title = "用户等级考核配置", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody UserLevelConfig userLevelConfig) {
        return toAjax(userLevelConfigService.save(userLevelConfig));
    }

    /**
     * 修改用户等级考核配置
     */
    @PreAuthorize("@ss.hasPermi('xms:userLevelConfig:edit')")
    @Log(title = "用户等级考核配置", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody UserLevelConfig userLevelConfig) {
        return toAjax(userLevelConfigService.updateRecordById(userLevelConfig));
    }

    /**
     * 删除用户等级考核配置
     */
    @PreAuthorize("@ss.hasPermi('xms:userLevelConfig:remove')")
    @Log(title = "用户等级考核配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(userLevelConfigService.removeByIds(Arrays.asList(ids)));
    }
}
