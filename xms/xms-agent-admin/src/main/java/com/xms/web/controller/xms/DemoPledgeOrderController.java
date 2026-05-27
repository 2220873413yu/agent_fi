package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.DemoPledgeOrder;
import com.xms.dao.entity.req.DemoPledgeBuyReq;
import com.xms.dao.service.IDemoPledgeOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 示例质押订单Controller。
 */
@RestController
@RequestMapping("/xms/demoPledgeOrder")
public class DemoPledgeOrderController extends BaseController {
	private final IDemoPledgeOrderService demoPledgeOrderService;

	public DemoPledgeOrderController(IDemoPledgeOrderService demoPledgeOrderService) {
		this.demoPledgeOrderService = demoPledgeOrderService;
	}

	/**
	 * 查询示例质押订单列表。
	 *
	 * @param demoPledgeOrder 查询条件
	 * @return 分页订单数据
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeOrder:list')")
	@GetMapping("/list")
	public TableDataInfo list(DemoPledgeOrder demoPledgeOrder) {
		startPage();
		List<DemoPledgeOrder> list = demoPledgeOrderService.selectDemoPledgeOrderList(demoPledgeOrder);
		return getDataTable(list);
	}

	/**
	 * 导出示例质押订单列表。
	 *
	 * @param response HTTP响应
	 * @param demoPledgeOrder 查询条件
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeOrder:export')")
	@Log(title = "示例质押订单", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, DemoPledgeOrder demoPledgeOrder) {
		List<DemoPledgeOrder> list = demoPledgeOrderService.selectDemoPledgeOrderList(demoPledgeOrder);
		ExcelUtil<DemoPledgeOrder> util = new ExcelUtil<>(DemoPledgeOrder.class);
		util.exportExcel(response, list, "示例质押订单数据");
	}

	/**
	 * 获取示例质押订单详细信息。
	 *
	 * @param id 订单ID
	 * @return 订单详情
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeOrder:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(demoPledgeOrderService.getById(id));
	}

	/**
	 * 后台演示购买示例质押套餐。
	 *
	 * @param req 购买请求
	 * @return 已支付订单
	 */
	@PreAuthorize("@ss.hasPermi('xms:demoPledgeOrder:buy')")
	@Log(title = "示例质押订单", businessType = BusinessType.INSERT)
	@PostMapping("/buy")
	@RepeatSubmit
	public AjaxResult buy(@RequestBody DemoPledgeBuyReq req) {
		return success(demoPledgeOrderService.buy(req));
	}
}
