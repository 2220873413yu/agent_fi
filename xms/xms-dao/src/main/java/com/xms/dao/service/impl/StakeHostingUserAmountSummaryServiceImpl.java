package com.xms.dao.service.impl;

import com.xms.common.constant.ConstantStatic;
import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.StakeHostingUserAmountSummary;
import com.xms.dao.entity.req.StakeHostingUserAmountAdjustReq;
import com.xms.dao.mapper.StakeHostingUserAmountSummaryMapper;
import com.xms.dao.service.IStakeHostingUserAmountSummaryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 托管用户累计金额Service业务层处理。
 */
@Service
public class StakeHostingUserAmountSummaryServiceImpl
	extends XmsDataServiceImpl<StakeHostingUserAmountSummaryMapper, StakeHostingUserAmountSummary>
	implements IStakeHostingUserAmountSummaryService {

	private static final Long GLOBAL_SUMMARY_ID = 1L;

	@Override
	public List<StakeHostingUserAmountSummary> selectStakeHostingUserAmountSummaryList(StakeHostingUserAmountSummary summary) {
		return baseMapper.selectStakeHostingUserAmountSummaryList(summary);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void increaseAmount(BigDecimal amount) {
		BigDecimal validAmount = validateAmount(amount);
		lambdaUpdate()
			.eq(StakeHostingUserAmountSummary::getId, GLOBAL_SUMMARY_ID)
			.setSql("total_amount = IFNULL(total_amount, 0) + " + validAmount.toPlainString())
			.set(StakeHostingUserAmountSummary::getUpdateTime, new Date())
			.update();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void decreaseAmount(BigDecimal amount) {
		BigDecimal validAmount = validateAmount(amount);
		lambdaUpdate()
			.eq(StakeHostingUserAmountSummary::getId, GLOBAL_SUMMARY_ID)
			.setSql("total_amount = GREATEST(IFNULL(total_amount, 0) - " + validAmount.toPlainString() + ", 0)")
			.set(StakeHostingUserAmountSummary::getUpdateTime, new Date())
			.update();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int manualAdjust(StakeHostingUserAmountAdjustReq req) {
		BigDecimal validAmount = validateAdjustAmount(req == null ? null : req.getAmount());
		String amountSql = validAmount.compareTo(BigDecimal.ZERO) > 0
			? "total_amount = IFNULL(total_amount, 0) + " + validAmount.toPlainString()
			: "total_amount = GREATEST(IFNULL(total_amount, 0) - " + validAmount.abs().toPlainString() + ", 0)";
		lambdaUpdate()
			.eq(StakeHostingUserAmountSummary::getId, GLOBAL_SUMMARY_ID)
			.setSql(amountSql)
			.set(StakeHostingUserAmountSummary::getUpdateTime, new Date())
			.update();
		return 1;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateRemark(StakeHostingUserAmountAdjustReq req) {
		lambdaUpdate()
			.eq(StakeHostingUserAmountSummary::getId, GLOBAL_SUMMARY_ID)
			.set(StakeHostingUserAmountSummary::getRemark, req == null ? null : req.getRemark())
			.set(StakeHostingUserAmountSummary::getUpdateTime, new Date())
			.update();
		return 1;
	}

	private BigDecimal validateAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("调整金额必须大于0");
		}
		return amount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	private BigDecimal validateAdjustAmount(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
			throw new ServiceException("调整金额不能为0");
		}
		BigDecimal validAmount = amount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if (validAmount.compareTo(BigDecimal.ZERO) == 0) {
			throw new ServiceException("调整金额不能为0");
		}
		return validAmount;
	}
}
