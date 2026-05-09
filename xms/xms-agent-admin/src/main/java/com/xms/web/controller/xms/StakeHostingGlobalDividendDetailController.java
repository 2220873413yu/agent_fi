package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingGlobalDividendDetail;
import com.xms.dao.service.IStakeHostingGlobalDividendDetailService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 托管全球分红明细Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingGlobalDividendDetail")
public class StakeHostingGlobalDividendDetailController extends BaseController {
	private final IStakeHostingGlobalDividendDetailService detailService;

	public StakeHostingGlobalDividendDetailController(IStakeHostingGlobalDividendDetailService detailService) {
		this.detailService = detailService;
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendDetail:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingGlobalDividendDetail detail) {
		startPage();
		List<StakeHostingGlobalDividendDetail> list = detailService.selectStakeHostingGlobalDividendDetailList(detail);
		return getDataTable(list);
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendDetail:export')")
	@Log(title = "托管全球分红明细", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingGlobalDividendDetail detail) {
		List<StakeHostingGlobalDividendDetail> list = detailService.selectStakeHostingGlobalDividendDetailList(detail);
		ExcelUtil<StakeHostingGlobalDividendDetail> util = new ExcelUtil<>(StakeHostingGlobalDividendDetail.class);
		util.exportExcel(response, list, "托管全球分红明细数据");
	}
}
