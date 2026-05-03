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
import com.xms.dao.domain.DiyStoreProductAttrValue;
import com.xms.dao.service.IDiyStoreProductAttrValueService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 商品属性值(SKU)Controller
 *
 * @author xms
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/xms/diyStoreProductAttrValue")
public class DiyStoreProductAttrValueController extends BaseController
{
    @Autowired
    private IDiyStoreProductAttrValueService diyStoreProductAttrValueService;

/**
 * 查询商品属性值(SKU)列表
 */
@PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttrValue:list')")
@GetMapping("/list")
    public TableDataInfo list(DiyStoreProductAttrValue diyStoreProductAttrValue)
    {
        startPage();
        List<DiyStoreProductAttrValue> list = diyStoreProductAttrValueService.selectDiyStoreProductAttrValueList(diyStoreProductAttrValue);
        return getDataTable(list);
    }

    /**
     * 导出商品属性值(SKU)列表
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttrValue:export')")
    @Log(title = "商品属性值(SKU)", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DiyStoreProductAttrValue diyStoreProductAttrValue)
    {
        List<DiyStoreProductAttrValue> list = diyStoreProductAttrValueService.selectDiyStoreProductAttrValueList(diyStoreProductAttrValue);
        ExcelUtil<DiyStoreProductAttrValue> util = new ExcelUtil<DiyStoreProductAttrValue>(DiyStoreProductAttrValue.class);
        util.exportExcel(response, list, "商品属性值(SKU)数据");
    }

    /**
     * 获取商品属性值(SKU)详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttrValue:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(diyStoreProductAttrValueService.getById(id));
    }

    /**
     * 新增商品属性值(SKU)
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttrValue:add')")
    @Log(title = "商品属性值(SKU)", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody DiyStoreProductAttrValue diyStoreProductAttrValue) {
        return toAjax(diyStoreProductAttrValueService.save(diyStoreProductAttrValue));
    }

    /**
     * 修改商品属性值(SKU)
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttrValue:edit')")
    @Log(title = "商品属性值(SKU)", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody DiyStoreProductAttrValue diyStoreProductAttrValue) {
        return toAjax(diyStoreProductAttrValueService.updateById(diyStoreProductAttrValue));
    }

    /**
     * 删除商品属性值(SKU)
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttrValue:remove')")
    @Log(title = "商品属性值(SKU)", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(diyStoreProductAttrValueService.removeByIds(Arrays.asList(ids)));
    }
}
