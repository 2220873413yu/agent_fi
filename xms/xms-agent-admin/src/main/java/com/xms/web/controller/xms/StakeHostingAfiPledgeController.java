package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingAfiPledge;
import com.xms.dao.service.IStakeHostingAfiPledgeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 托管订单AFI质押记录Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingAfiPledge")
public class StakeHostingAfiPledgeController extends BaseController {
	private final IStakeHostingAfiPledgeService pledgeService;

	public StakeHostingAfiPledgeController(IStakeHostingAfiPledgeService pledgeService) {
		this.pledgeService = pledgeService;
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiPledge:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingAfiPledge pledge) {
		startPage();
		List<StakeHostingAfiPledge> list = pledgeService.selectStakeHostingAfiPledgeList(pledge);
		return getDataTable(list);
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiPledge:export')")
	@Log(title = "托管订单AFI质押记录", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingAfiPledge pledge) {
		List<StakeHostingAfiPledge> list = pledgeService.selectStakeHostingAfiPledgeList(pledge);
		ExcelUtil<StakeHostingAfiPledge> util = new ExcelUtil<>(StakeHostingAfiPledge.class);
		util.exportExcel(response, list, "托管订单AFI质押记录数据");
	}

	@PreAuthorize("@ss.hasPermi('xms:stakeHostingAfiPledge:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(pledgeService.getById(id));
	}
}
