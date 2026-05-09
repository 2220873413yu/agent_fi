package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingGlobalDividendBatch;
import com.xms.dao.service.IStakeHostingGlobalDividendBatchService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 托管全球分红批次Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingGlobalDividendBatch")
public class StakeHostingGlobalDividendBatchController extends BaseController {
	private final IStakeHostingGlobalDividendBatchService batchService;

	public StakeHostingGlobalDividendBatchController(IStakeHostingGlobalDividendBatchService batchService) {
		this.batchService = batchService;
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendBatch:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingGlobalDividendBatch batch) {
		startPage();
		List<StakeHostingGlobalDividendBatch> list = batchService.selectStakeHostingGlobalDividendBatchList(batch);
		return getDataTable(list);
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendBatch:export')")
	@Log(title = "托管全球分红批次", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingGlobalDividendBatch batch) {
		List<StakeHostingGlobalDividendBatch> list = batchService.selectStakeHostingGlobalDividendBatchList(batch);
		ExcelUtil<StakeHostingGlobalDividendBatch> util = new ExcelUtil<>(StakeHostingGlobalDividendBatch.class);
		util.exportExcel(response, list, "托管全球分红批次数据");
	}
}
