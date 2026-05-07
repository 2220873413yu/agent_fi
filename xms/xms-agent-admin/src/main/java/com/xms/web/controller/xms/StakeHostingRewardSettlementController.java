package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.common.utils.poi.ExcelUtil;
import com.xms.dao.domain.StakeHostingRewardSettlement;
import com.xms.dao.service.IStakeHostingRewardSettlementService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 托管奖励结算明细Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingRewardSettlement")
public class StakeHostingRewardSettlementController extends BaseController {
	private final IStakeHostingRewardSettlementService stakeHostingRewardSettlementService;

	public StakeHostingRewardSettlementController(IStakeHostingRewardSettlementService stakeHostingRewardSettlementService) {
		this.stakeHostingRewardSettlementService = stakeHostingRewardSettlementService;
	}

	/**
	 * 查询托管奖励结算明细列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingRewardSettlement:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingRewardSettlement settlement) {
		startPage();
		List<StakeHostingRewardSettlement> list = stakeHostingRewardSettlementService.selectStakeHostingRewardSettlementList(settlement);
		return getDataTable(list);
	}

	/**
	 * 导出托管奖励结算明细列表
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingRewardSettlement:export')")
	@Log(title = "托管奖励结算明细", businessType = BusinessType.EXPORT)
	@PostMapping("/export")
	public void export(HttpServletResponse response, StakeHostingRewardSettlement settlement) {
		List<StakeHostingRewardSettlement> list = stakeHostingRewardSettlementService.selectStakeHostingRewardSettlementList(settlement);
		ExcelUtil<StakeHostingRewardSettlement> util = new ExcelUtil<>(StakeHostingRewardSettlement.class);
		util.exportExcel(response, list, "托管奖励结算明细数据");
	}
}
