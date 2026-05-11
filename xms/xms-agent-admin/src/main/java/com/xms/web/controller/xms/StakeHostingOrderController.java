package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.entity.dto.StakeHostingOrderListDto;
import com.xms.dao.service.IStakeHostingOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 托管订单Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingOrder")
public class StakeHostingOrderController extends BaseController {
	private final IStakeHostingOrderService stakeHostingOrderService;

	public StakeHostingOrderController(IStakeHostingOrderService stakeHostingOrderService) {
		this.stakeHostingOrderService = stakeHostingOrderService;
	}

	/**
	 * 查询托管订单列表。
	 *
	 * @param query 托管订单查询条件
	 * @return 后台分页列表，包含AFI质押比例和加速倍率展示字段
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingOrder:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingOrderListDto query) {
		startPage();
		List<StakeHostingOrderListDto> list = stakeHostingOrderService.selectStakeHostingOrderDtoList(query);
		return getDataTable(list);
	}

	/**
	 * 导出托管订单列表。
	 *
	 * @param response HTTP响应
	 * @param query 托管订单查询条件
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingOrder:export')")
	@Log(title = "托管订单", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingOrderListDto query) {
		List<StakeHostingOrderListDto> list = stakeHostingOrderService.selectStakeHostingOrderDtoList(query);
		ExcelUtil<StakeHostingOrderListDto> util = new ExcelUtil<>(StakeHostingOrderListDto.class);
		util.exportExcel(response, list, "托管订单数据");
	}

	/**
	 * 获取托管订单详细信息
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingOrder:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(stakeHostingOrderService.getById(id));
	}

	/**
	 * 后台拨付托管订单
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingOrder:add')")
	@Log(title = "后台拨付托管订单", businessType = BusinessType.INSERT)
	@PostMapping
	@RepeatSubmit
	public AjaxResult add(@RequestBody StakeHostingOrder stakeHostingOrder) {
		return toAjax(stakeHostingOrderService.createAdminGrantOrder(stakeHostingOrder));
	}

	/**
	 * 修改托管订单
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingOrder:edit')")
	@Log(title = "托管订单", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody StakeHostingOrder stakeHostingOrder) {
		return toAjax(stakeHostingOrderService.updateById(stakeHostingOrder));
	}

	/**
	 * 删除托管订单
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingOrder:remove')")
	@Log(title = "托管订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids) {
		return toAjax(stakeHostingOrderService.removeByIds(Arrays.asList(ids)));
	}
}
