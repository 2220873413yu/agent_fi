package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;
import com.xms.dao.service.IStakeHostingGlobalDividendWeightSnapshotService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 托管全球分红权重快照Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingGlobalDividendWeightSnapshot")
public class StakeHostingGlobalDividendWeightSnapshotController extends BaseController {
	private final IStakeHostingGlobalDividendWeightSnapshotService snapshotService;

	public StakeHostingGlobalDividendWeightSnapshotController(IStakeHostingGlobalDividendWeightSnapshotService snapshotService) {
		this.snapshotService = snapshotService;
	}

	@GetMapping("/list")
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendWeightSnapshot:list')")
	public TableDataInfo list(StakeHostingGlobalDividendWeightSnapshot snapshot) {
		startPage();
		List<StakeHostingGlobalDividendWeightSnapshot> list = snapshotService.selectStakeHostingGlobalDividendWeightSnapshotList(snapshot);
		return getDataTable(list);
	}

	@PostMapping("/export")
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendWeightSnapshot:export')")
	@Log(title = "托管全球分红权重快照", businessType = BusinessType.EXPORT)
	public void export(HttpServletResponse response, StakeHostingGlobalDividendWeightSnapshot snapshot) {
		List<StakeHostingGlobalDividendWeightSnapshot> list = snapshotService.selectStakeHostingGlobalDividendWeightSnapshotList(snapshot);
		ExcelUtil<StakeHostingGlobalDividendWeightSnapshot> util = new ExcelUtil<>(StakeHostingGlobalDividendWeightSnapshot.class);
		util.exportExcel(response, list, "托管全球分红权重快照数据");
	}
}
