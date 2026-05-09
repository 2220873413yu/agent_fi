package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingGlobalDividendPoolLog;
import com.xms.dao.service.IStakeHostingGlobalDividendPoolLogService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 托管全球分红奖池流水Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingGlobalDividendPoolLog")
public class StakeHostingGlobalDividendPoolLogController extends BaseController {
	private final IStakeHostingGlobalDividendPoolLogService poolLogService;

	public StakeHostingGlobalDividendPoolLogController(IStakeHostingGlobalDividendPoolLogService poolLogService) {
		this.poolLogService = poolLogService;
	}

	/**
	 * 查询托管全球分红奖池流水列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendPoolLog:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingGlobalDividendPoolLog log) {
		startPage();
		List<StakeHostingGlobalDividendPoolLog> list = poolLogService.selectStakeHostingGlobalDividendPoolLogList(log);
		return getDataTable(list);
	}

	/**
	 * 导出托管全球分红奖池流水列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendPoolLog:export')")
	@Log(title = "托管全球分红奖池流水", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingGlobalDividendPoolLog log) {
		List<StakeHostingGlobalDividendPoolLog> list = poolLogService.selectStakeHostingGlobalDividendPoolLogList(log);
		ExcelUtil<StakeHostingGlobalDividendPoolLog> util = new ExcelUtil<>(StakeHostingGlobalDividendPoolLog.class);
		util.exportExcel(response, list, "托管全球分红奖池流水数据");
	}
}
