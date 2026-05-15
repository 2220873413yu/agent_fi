package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;
import com.xms.dao.service.IStakeHostingGlobalDividendWeightSnapshotService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for stake hosting global dividend weekly weight snapshots.
 */
@RestController
@RequestMapping("/xms/stakeHostingGlobalDividendWeightSnapshot")
public class StakeHostingGlobalDividendWeightSnapshotController extends BaseController {
	private final IStakeHostingGlobalDividendWeightSnapshotService snapshotService;

	public StakeHostingGlobalDividendWeightSnapshotController(IStakeHostingGlobalDividendWeightSnapshotService snapshotService) {
		this.snapshotService = snapshotService;
	}

	/**
	 * Queries global dividend weekly weight snapshots for the admin list page.
	 *
	 * @param snapshot query conditions
	 * @return paged snapshot rows
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendWeightSnapshot:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingGlobalDividendWeightSnapshot snapshot) {
		startPage();
		List<StakeHostingGlobalDividendWeightSnapshot> list = snapshotService.selectStakeHostingGlobalDividendWeightSnapshotList(snapshot);
		return getDataTable(list);
	}

	/**
	 * Exports global dividend weekly weight snapshots using the same filters as the list page.
	 *
	 * @param response HTTP response for Excel output
	 * @param snapshot query conditions
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendWeightSnapshot:export')")
	@Log(title = "全球分红权重快照", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingGlobalDividendWeightSnapshot snapshot) {
		List<StakeHostingGlobalDividendWeightSnapshot> list = snapshotService.selectStakeHostingGlobalDividendWeightSnapshotList(snapshot);
		ExcelUtil<StakeHostingGlobalDividendWeightSnapshot> util = new ExcelUtil<>(StakeHostingGlobalDividendWeightSnapshot.class);
		util.exportExcel(response, list, "全球分红权重快照数据");
	}
}
