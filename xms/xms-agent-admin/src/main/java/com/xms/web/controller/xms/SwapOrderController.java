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
import com.xms.dao.domain.SwapOrder;
import com.xms.dao.service.ISwapOrderService;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.common.core.page.TableDataInfo;

/**
 * swap订单Controller
 *
 * @author xms
 * @date 2026-01-04
 */
@RestController
@RequestMapping("/xms/swapOrder")
public class SwapOrderController extends BaseController
{
    @Autowired
    private ISwapOrderService swapOrderService;

	/**
	 * 查询swap订单列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:swapOrder:list')")
	@GetMapping("/list")
    public TableDataInfo list(SwapOrder swapOrder)
    {
        startPage();
        List<SwapOrder> list = swapOrderService.selectSwapOrderList(swapOrder);
        return getDataTable(list);
    }

    /**
     * 导出swap订单列表
     */
    @PreAuthorize("@ss.hasPermi('xms:swapOrder:export')")
    @Log(title = "swap订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SwapOrder swapOrder)
    {
        List<SwapOrder> list = swapOrderService.selectSwapOrderList(swapOrder);
        ExcelUtil<SwapOrder> util = new ExcelUtil<SwapOrder>(SwapOrder.class);
        util.exportExcel(response, list, "swap订单数据");
    }

    /**
     * 获取swap订单详细信息
     */
    @PreAuthorize("@ss.hasPermi('xms:swapOrder:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(swapOrderService.getById(id));
    }

    /**
     * 新增swap订单
     */
    @PreAuthorize("@ss.hasPermi('xms:swapOrder:add')")
    @Log(title = "swap订单", businessType = BusinessType.INSERT)
    @PostMapping
    @RepeatSubmit
    public AjaxResult add(@RequestBody SwapOrder swapOrder) {
        return toAjax(swapOrderService.save(swapOrder));
    }

    /**
     * 修改swap订单
     */
    @PreAuthorize("@ss.hasPermi('xms:swapOrder:edit')")
    @Log(title = "swap订单", businessType = BusinessType.UPDATE)
    @PutMapping
    @RepeatSubmit
    public AjaxResult edit(@RequestBody SwapOrder swapOrder) {
        return toAjax(swapOrderService.updateById(swapOrder));
    }

    /**
     * 删除swap订单
     */
    @PreAuthorize("@ss.hasPermi('xms:swapOrder:remove')")
    @Log(title = "swap订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(swapOrderService.removeByIds(Arrays.asList(ids)));
    }
}
