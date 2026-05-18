package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.PolymarketMarket;
import com.xms.dao.service.IPolymarketMarketService;
import com.xms.dao.service.IPolymarketOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台Polymarket市场聚合管理接口。
 *
 * <p>用于查看市场级下单总额、结算状态和开奖结果，并支持后台手动触发单个市场结算。</p>
 */
@RestController
@RequestMapping("/xms/polymarketMarket")
public class PolymarketMarketController extends BaseController {

	private final IPolymarketMarketService polymarketMarketService;
	private final IPolymarketOrderService polymarketOrderService;

	public PolymarketMarketController(IPolymarketMarketService polymarketMarketService,
									  IPolymarketOrderService polymarketOrderService) {
		this.polymarketMarketService = polymarketMarketService;
		this.polymarketOrderService = polymarketOrderService;
	}

	/**
	 * 后台分页查询Polymarket市场聚合列表。
	 *
	 * @param polymarketMarket 查询筛选条件
	 * @return 分页市场列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketMarket:list')")
	@GetMapping("/list")
	public TableDataInfo list(PolymarketMarket polymarketMarket) {
		startPage();
		List<PolymarketMarket> list = polymarketMarketService.selectPolymarketMarketList(polymarketMarket);
		return getDataTable(list);
	}

	/**
	 * 导出Polymarket市场聚合数据。
	 *
	 * @param response HTTP响应
	 * @param polymarketMarket 查询筛选条件
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketMarket:export')")
	@Log(title = "Polymarket市场", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, PolymarketMarket polymarketMarket) {
		List<PolymarketMarket> list = polymarketMarketService.selectPolymarketMarketList(polymarketMarket);
		ExcelUtil<PolymarketMarket> util = new ExcelUtil<>(PolymarketMarket.class);
		util.exportExcel(response, list, "Polymarket市场数据");
	}

	/**
	 * 查询单个Polymarket市场聚合详情。
	 *
	 * @param id 市场主键ID
	 * @return 市场详情
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketMarket:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(polymarketMarketService.getById(id));
	}

	/**
	 * 后台手动触发单个市场结算。
	 *
	 * <p>用于当前延迟队列发送暂未启用时的临时处理入口。待结算市场会先派发为结算中，结算中市场会直接尝试处理。</p>
	 *
	 * @param marketSlug Polymarket市场slug
	 * @return 是否触发状态更新
	 */
	@PreAuthorize("@ss.hasPermi('xms:polymarketMarket:edit')")
	@Log(title = "Polymarket市场结算", businessType = BusinessType.UPDATE)
	@PostMapping("/settle/{marketSlug}")
	@RepeatSubmit
	public AjaxResult settleMarket(@PathVariable("marketSlug") String marketSlug) {
		return success(polymarketOrderService.settleMarketBySlug(marketSlug));
	}
}
