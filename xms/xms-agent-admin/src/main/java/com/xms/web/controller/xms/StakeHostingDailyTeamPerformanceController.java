package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingDailyTeamPerformance;
import com.xms.dao.service.IStakeHostingDailyTeamPerformanceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 托管G7每日团队新增业绩与静态收益率快照Controller。
 *
 * <p>该后台页面只提供查询和导出能力，用于核对101任务或测试任务生成的G_day、Gsmooth和基础静态收益率，
 * 不允许后台直接新增、修改或删除快照数据。</p>
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingDailyTeamPerformance")
public class StakeHostingDailyTeamPerformanceController extends BaseController {
	private final IStakeHostingDailyTeamPerformanceService performanceService;

	public StakeHostingDailyTeamPerformanceController(IStakeHostingDailyTeamPerformanceService performanceService) {
		this.performanceService = performanceService;
	}

	/**
	 * 查询托管G7每日团队新增业绩与静态收益率快照列表。
	 *
	 * @param performance 查询条件，支持用户ID、钱包地址、统计日、收益率来源和计算状态
	 * @return 分页后的G7每日快照列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingDailyTeamPerformance:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingDailyTeamPerformance performance) {
		startPage();
		List<StakeHostingDailyTeamPerformance> list = performanceService.selectStakeHostingDailyTeamPerformanceList(performance);
		return getDataTable(list);
	}

	/**
	 * 导出托管G7每日团队新增业绩与静态收益率快照。
	 *
	 * @param response HTTP响应
	 * @param performance 查询条件
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingDailyTeamPerformance:export')")
	@Log(title = "托管G7每日快照", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingDailyTeamPerformance performance) {
		List<StakeHostingDailyTeamPerformance> list = performanceService.selectStakeHostingDailyTeamPerformanceList(performance);
		ExcelUtil<StakeHostingDailyTeamPerformance> util = new ExcelUtil<>(StakeHostingDailyTeamPerformance.class);
		util.exportExcel(response, list, "托管G7每日快照数据");
	}

	/**
	 * 查询托管G7每日快照详情。
	 *
	 * @param id 快照ID
	 * @return G7每日快照详情
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingDailyTeamPerformance:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(performanceService.getById(id));
	}
}
