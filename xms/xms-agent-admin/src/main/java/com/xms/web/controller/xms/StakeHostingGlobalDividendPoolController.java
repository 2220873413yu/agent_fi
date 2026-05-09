package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.enums.BusinessType;
import com.xms.dao.domain.StakeHostingGlobalDividendPoolAdjustBo;
import com.xms.dao.service.IStakeHostingGlobalDividendPoolService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 托管全球分红奖池Controller
 *
 * @author xms
 */
@RestController
@RequestMapping("/xms/stakeHostingGlobalDividendPool")
public class StakeHostingGlobalDividendPoolController extends BaseController {
	private final IStakeHostingGlobalDividendPoolService poolService;

	public StakeHostingGlobalDividendPoolController(IStakeHostingGlobalDividendPoolService poolService) {
		this.poolService = poolService;
	}

	/**
	 * 查询托管全球分红奖池
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendPool:list')")
	@GetMapping("/info")
	public AjaxResult info() {
		return success(poolService.getOrInitPool());
	}

	/**
	 * 手动调增/调减托管全球分红奖池
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingGlobalDividendPool:adjust')")
	@Log(title = "托管全球分红奖池调账", businessType = BusinessType.UPDATE)
	@PostMapping("/adjust")
	@RepeatSubmit
	public AjaxResult adjust(@RequestBody StakeHostingGlobalDividendPoolAdjustBo req) {
		return success(poolService.adjustPool(req.getFlowType(), req.getAmount(), req.getRemark(), getUsername()));
	}
}
