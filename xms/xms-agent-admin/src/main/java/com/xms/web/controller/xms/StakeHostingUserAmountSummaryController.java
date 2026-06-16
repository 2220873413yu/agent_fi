package com.xms.web.controller.xms;

import com.xms.common.annotation.Log;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.core.controller.BaseController;
import com.xms.common.core.domain.AjaxResult;
import com.xms.common.core.page.TableDataInfo;
import com.xms.common.enums.BusinessType;
import com.xms.dao.domain.StakeHostingUserAmountSummary;
import com.xms.dao.entity.req.StakeHostingUserAmountAdjustReq;
import com.xms.dao.service.IStakeHostingUserAmountSummaryService;
import com.xms.web.service.PermissionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

/**
 * 全平台托管累计金额Controller。
 */
@RestController
@RequestMapping("/xms/stakeHostingUserAmountSummary")
public class StakeHostingUserAmountSummaryController extends BaseController {
	private static final String PERMISSION_INCREASE = "xms:stakeHostingUserAmountSummary:increase";
	private static final String PERMISSION_DECREASE = "xms:stakeHostingUserAmountSummary:decrease";

	private final IStakeHostingUserAmountSummaryService summaryService;
	private final PermissionService permissionService;

	public StakeHostingUserAmountSummaryController(IStakeHostingUserAmountSummaryService summaryService,
												   PermissionService permissionService) {
		this.summaryService = summaryService;
		this.permissionService = permissionService;
	}

	/**
	 * 查询全平台托管累计金额列表。
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingUserAmountSummary:list')")
	@GetMapping("/list")
	public TableDataInfo list(StakeHostingUserAmountSummary summary) {
		startPage();
		List<StakeHostingUserAmountSummary> list = summaryService.selectStakeHostingUserAmountSummaryList(summary);
		return getDataTable(list);
	}

	/**
	 * 获取全平台托管累计金额详情。
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingUserAmountSummary:query')")
	@GetMapping(value = "/{id}")
	public AjaxResult getInfo(@PathVariable("id") Long id) {
		return success(summaryService.getById(id));
	}

	/**
	 * 后台手动调整全平台托管累计金额，正数增加，负数扣除。
	 */
	@PreAuthorize("@ss.hasAnyPermi('xms:stakeHostingUserAmountSummary:increase,xms:stakeHostingUserAmountSummary:decrease')")
	@Log(title = "全平台托管累计金额调整", businessType = BusinessType.UPDATE)
	@PutMapping("/manualAdjust")
	@RepeatSubmit
	public AjaxResult manualAdjust(@RequestBody StakeHostingUserAmountAdjustReq req) {
		AjaxResult permissionResult = checkAdjustPermission(req);
		if (permissionResult != null) {
			return permissionResult;
		}
		return toAjax(summaryService.manualAdjust(req));
	}

	/**
	 * 修改全平台托管累计金额备注。
	 */
	@PreAuthorize("@ss.hasPermi('xms:stakeHostingUserAmountSummary:edit')")
	@Log(title = "全平台托管累计金额备注", businessType = BusinessType.UPDATE)
	@PutMapping("/remark")
	@RepeatSubmit
	public AjaxResult updateRemark(@RequestBody StakeHostingUserAmountAdjustReq req) {
		return toAjax(summaryService.updateRemark(req));
	}

	private AjaxResult checkAdjustPermission(StakeHostingUserAmountAdjustReq req) {
		BigDecimal amount = req == null ? null : req.getAmount();
		if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
			return null;
		}
		if (amount.compareTo(BigDecimal.ZERO) > 0 && !permissionService.hasPermi(PERMISSION_INCREASE)) {
			return AjaxResult.error("没有托管累计金额增加权限");
		}
		if (amount.compareTo(BigDecimal.ZERO) < 0 && !permissionService.hasPermi(PERMISSION_DECREASE)) {
			return AjaxResult.error("没有托管累计金额扣除权限");
		}
		return null;
	}
}
