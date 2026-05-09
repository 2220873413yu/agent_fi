package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingWeeklyCommunityPerformance;
import com.xms.dao.service.IStakeHostingWeeklyCommunityPerformanceService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 托管每周新增小区业绩Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingWeeklyCommunityPerformance")
public class StakeHostingWeeklyCommunityPerformanceController extends BaseController {
	private final IStakeHostingWeeklyCommunityPerformanceService performanceService;

	public StakeHostingWeeklyCommunityPerformanceController(IStakeHostingWeeklyCommunityPerformanceService performanceService) {
		this.performanceService = performanceService;
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingWeeklyCommunityPerformance:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingWeeklyCommunityPerformance performance) {
		startPage();
		List<StakeHostingWeeklyCommunityPerformance> list = performanceService.selectStakeHostingWeeklyCommunityPerformanceList(performance);
		return getDataTable(list);
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingWeeklyCommunityPerformance:export')")
	@Log(title = "托管每周新增小区业绩", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingWeeklyCommunityPerformance performance) {
		List<StakeHostingWeeklyCommunityPerformance> list = performanceService.selectStakeHostingWeeklyCommunityPerformanceList(performance);
		ExcelUtil<StakeHostingWeeklyCommunityPerformance> util = new ExcelUtil<>(StakeHostingWeeklyCommunityPerformance.class);
		util.exportExcel(response, list, "托管每周新增小区业绩数据");
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingWeeklyCommunityPerformance:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(performanceService.getById(id));
	}
}
