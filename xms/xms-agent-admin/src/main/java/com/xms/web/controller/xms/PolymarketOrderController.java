package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.PolymarketOrder;
import com.xms.dao.service.IPolymarketOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * 后台Polymarket内部订单管理接口。
 *
 * <p>用于查询订单、导出订单、人工复核，以及手动触发待结算订单处理。</p>
 */
@RestController
@RequestMapping("/xms/polymarketOrder")
public class PolymarketOrderController extends BaseController {

	private final IPolymarketOrderService polymarketOrderService;

	public PolymarketOrderController(IPolymarketOrderService polymarketOrderService) {
		this.polymarketOrderService = polymarketOrderService;
	}

	/**
	 * 后台分页查询Polymarket内部订单。
	 *
	 * @param polymarketOrder 查询筛选条件
	 * @return 分页订单列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketOrder:list')")
	@GetMapping("/list")
	public TableDataInfo list(PolymarketOrder polymarketOrder) {
		startPage();
		List<PolymarketOrder> list = polymarketOrderService.selectPolymarketOrderList(polymarketOrder);
		return getDataTable(list);
	}

	/**
	 * 导出Polymarket内部订单。
	 *
	 * @param response HTTP响应
	 * @param polymarketOrder 查询筛选条件
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketOrder:export')")
	@Log(title = "Polymarket订单", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, PolymarketOrder polymarketOrder) {
		List<PolymarketOrder> list = polymarketOrderService.selectPolymarketOrderList(polymarketOrder);
		ExcelUtil<PolymarketOrder> util = new ExcelUtil<>(PolymarketOrder.class);
		util.exportExcel(response, list, "Polymarket订单数据");
	}

	/**
	 * 查询一笔Polymarket内部订单详情。
	 *
	 * @param id 订单主键ID
	 * @return 订单详情
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketOrder:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(polymarketOrderService.getById(id));
	}

	/**
	 * 后台更新Polymarket订单复核相关字段。
	 *
	 * <p>该接口用于人工复核不明确结果。它只更新订单字段，不自动做钱包兑付；自动兑付由结算服务处理。</p>
	 *
	 * @param polymarketOrder 要更新的订单字段
	 * @return 更新结果
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketOrder:edit')")
	@Log(title = "Polymarket订单", businessType = BusinessType.UPDATE)
	@PutMapping
	@RepeatSubmit
	public AjaxResult edit(@RequestBody PolymarketOrder polymarketOrder) {
		polymarketOrder.setUpdateTime(new Date());
		return toAjax(polymarketOrderService.updateById(polymarketOrder));
	}

	/**
	 * 手动触发待结算订单处理。
	 *
	 * <p>用于后台临时补跑或排查定时任务。正常情况下由Quartz任务自动调用。</p>
	 *
	 * @param limit 本次最多处理的待结算订单数
	 * @return 被更新的订单数量
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketOrder:edit')")
	@Log(title = "Polymarket订单结算", businessType = BusinessType.UPDATE)
	@PostMapping("/settlePending")
	@RepeatSubmit
	public AjaxResult settlePending(Integer limit) {
		return success(polymarketOrderService.settlePendingOrders(limit));
	}
}
