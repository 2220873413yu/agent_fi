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
import com.xms.dao.domain.DiyStoreProductAttr;
import com.xms.dao.service.IDiyStoreProductAttrService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 商品属性Controller
 *
 * @author xms
 * @date 2026-04-08
 */
@RestController
@RequestMapping("/xms/diyStoreProductAttr")
public class DiyStoreProductAttrController extends BaseController
{
    @Autowired
    private IDiyStoreProductAttrService diyStoreProductAttrService;

/**
 * 查询商品属性列表
 */
@PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttr:list')")
@GetMapping("/list")
    public TableDataInfo list(DiyStoreProductAttr diyStoreProductAttr)
    {
        startPage();
        List<DiyStoreProductAttr> list = diyStoreProductAttrService.selectDiyStoreProductAttrList(diyStoreProductAttr);
        return getDataTable(list);
    }

    /**
     * 导出商品属性列表
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttr:export')")
    @Log(title = "商品属性", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DiyStoreProductAttr diyStoreProductAttr)
    {
        List<DiyStoreProductAttr> list = diyStoreProductAttrService.selectDiyStoreProductAttrList(diyStoreProductAttr);
        ExcelUtil<DiyStoreProductAttr> util = new ExcelUtil<DiyStoreProductAttr>(DiyStoreProductAttr.class);
        util.exportExcel(response, list, "商品属性数据");
    }

    /**
     * 获取商品属性详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttr:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) {
        return success(diyStoreProductAttrService.getById(id));
    }

    /**
     * 新增商品属性
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttr:add')")
    @Log(title = "商品属性", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody DiyStoreProductAttr diyStoreProductAttr) {
        return toAjax(diyStoreProductAttrService.save(diyStoreProductAttr));
    }

    /**
     * 修改商品属性
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttr:edit')")
    @Log(title = "商品属性", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody DiyStoreProductAttr diyStoreProductAttr) {
        return toAjax(diyStoreProductAttrService.updateById(diyStoreProductAttr));
    }

    /**
     * 删除商品属性
     */
    @PreAuthorize("@ss.hasPermi('xms:diyStoreProductAttr:remove')")
    @Log(title = "商品属性", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) {
        return toAjax(diyStoreProductAttrService.removeByIds(Arrays.asList(ids)));
    }
}
