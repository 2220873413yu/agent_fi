package com.xms.web.controller.xms;

import java.util.Arrays;
import java.util.List;

import com.xms.common.annotation.RepeatSubmit;
import com.xms.dao.entity.req.AllocateNodePackReq;
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
import com.xms.dao.domain.NodePackageOrder;
import com.xms.dao.service.INodePackageOrderService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * 节点购买记录Controller
 *
 * @author xms
 * @date 2026-04-28
 */
@RestController
@RequestMapping("/xms/nodePackageOrder")
public class NodePackageOrderController extends BaseController
{
    @Autowired
    private INodePackageOrderService nodePackageOrderService;

/**
 * 查询节点购买记录列表
 */
@PreAuthorize("@ss.hasPermi('xms:nodePackageOrder:list')")
@GetMapping("/list")
    public TableDataInfo list(NodePackageOrder nodePackageOrder)
    {
        startPage();
        List<NodePackageOrder> list = nodePackageOrderService.selectNodePackageOrderList(nodePackageOrder);
        return getDataTable(list);
    }

    /**
     * 导出节点购买记录列表
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackageOrder:export')")
    @Log(title = "节点购买记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, NodePackageOrder nodePackageOrder)
    {
        List<NodePackageOrder> list = nodePackageOrderService.selectNodePackageOrderList(nodePackageOrder);
        ExcelUtil<NodePackageOrder> util = new ExcelUtil<NodePackageOrder>(NodePackageOrder.class);
        util.exportExcel(response, list, "节点购买记录数据");
    }

    /**
     * 获取节点购买记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackageOrder:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(nodePackageOrderService.getById(id));
    }

    /**
     * 后台拨付节点
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackageOrder:add')")
    @Log(title = "后台拨付节点", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody AllocateNodePackReq req) {
        return toAjax(nodePackageOrderService.saveNodePackageOrder(req));
    }

    /**
     * 修改节点购买记录
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackageOrder:edit')")
    @Log(title = "节点购买记录", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody NodePackageOrder nodePackageOrder) {
        return toAjax(nodePackageOrderService.updateById(nodePackageOrder));
    }

    /**
     * 删除节点购买记录
     */
    @PreAuthorize("@ss.hasPermi('xms:nodePackageOrder:remove')")
    @Log(title = "节点购买记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(nodePackageOrderService.removeByIds(Arrays.asList(ids)));
    }
}
