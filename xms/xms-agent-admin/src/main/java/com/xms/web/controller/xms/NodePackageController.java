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
import com.xms.dao.domain.NodePackage;
import com.xms.dao.service.INodePackageService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 节点套餐Controller
 *
 * @author xms
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/xms/nodePackage")
public class NodePackageController extends BaseController
{
    @Autowired
    private INodePackageService nodePackageService;

/**
 * 查询节点套餐列表
 */
@PreAuthorize("@ss.hasPermi('xms:nodePackage:list')")
@GetMapping("/list")
    public TableDataInfo list(NodePackage nodePackage)
    {
        startPage();
        List<NodePackage> list = nodePackageService.selectNodePackageList(nodePackage);
        return getDataTable(list);
    }

    /**
     * 导出节点套餐列表
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackage:export')")
    @Log(title = "节点套餐", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NodePackage nodePackage)
    {
        List<NodePackage> list = nodePackageService.selectNodePackageList(nodePackage);
        ExcelUtil<NodePackage> util = new ExcelUtil<NodePackage>(NodePackage.class);
        util.exportExcel(response, list, "节点套餐数据");
    }

    /**
     * 获取节点套餐详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackage:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(nodePackageService.getById(id));
    }

    /**
     * 新增节点套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackage:add')")
    @Log(title = "节点套餐", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody NodePackage nodePackage) {
        return toAjax(nodePackageService.save(nodePackage));
    }

    /**
     * 修改节点套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackage:edit')")
    @Log(title = "节点套餐", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody NodePackage nodePackage) {
        return toAjax(nodePackageService.updateNodePackageById(nodePackage));
    }

    /**
     * 删除节点套餐
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackage:remove')")
    @Log(title = "节点套餐", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(nodePackageService.removeByIds(Arrays.asList(ids)));
    }
}
