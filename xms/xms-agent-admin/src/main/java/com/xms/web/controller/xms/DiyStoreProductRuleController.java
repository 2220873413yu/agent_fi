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
import com.xms.dao.domain.DiyStoreProductRule;
import com.xms.dao.service.IDiyStoreProductRuleService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 商品规则值Controller
 *
 * @author xms
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/xms/diyStoreProductRule")
public class DiyStoreProductRuleController extends BaseController
{
    @Autowired
    private IDiyStoreProductRuleService diyStoreProductRuleService;

/**
 * 查询商品规则值列表
 */
@PreAuthorize("@ss.hasPermi('xms:diyStoreProductRule:list')")
@GetMapping("/list")
    public TableDataInfo list(DiyStoreProductRule diyStoreProductRule)
    {
        startPage();
        List<DiyStoreProductRule> list = diyStoreProductRuleService.selectDiyStoreProductRuleList(diyStoreProductRule);
        return getDataTable(list);
    }

    /**
     * 导出商品规则值列表
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductRule:export')")
    @Log(title = "商品规则值", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DiyStoreProductRule diyStoreProductRule)
    {
        List<DiyStoreProductRule> list = diyStoreProductRuleService.selectDiyStoreProductRuleList(diyStoreProductRule);
        ExcelUtil<DiyStoreProductRule> util = new ExcelUtil<DiyStoreProductRule>(DiyStoreProductRule.class);
        util.exportExcel(response, list, "商品规则值数据");
    }

    /**
     * 获取商品规则值详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductRule:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(diyStoreProductRuleService.getById(id));
    }

    /**
     * 新增商品规则值
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductRule:add')")
    @Log(title = "商品规则值", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody DiyStoreProductRule diyStoreProductRule) {
        return toAjax(diyStoreProductRuleService.save(diyStoreProductRule));
    }

    /**
     * 修改商品规则值
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductRule:edit')")
    @Log(title = "商品规则值", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody DiyStoreProductRule diyStoreProductRule) {
        return toAjax(diyStoreProductRuleService.updateById(diyStoreProductRule));
    }

    /**
     * 删除商品规则值
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductRule:remove')")
    @Log(title = "商品规则值", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(diyStoreProductRuleService.removeByIds(Arrays.asList(ids)));
    }
}
