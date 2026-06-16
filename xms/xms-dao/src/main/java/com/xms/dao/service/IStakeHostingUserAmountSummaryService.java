package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingUserAmountSummary;
import com.xms.dao.entity.req.StakeHostingUserAmountAdjustReq;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管用户累计金额Service接口。
 */
public interface IStakeHostingUserAmountSummaryService extends XmsDataService<StakeHostingUserAmountSummary> {
	List<StakeHostingUserAmountSummary> selectStakeHostingUserAmountSummaryList(StakeHostingUserAmountSummary summary);

	void increaseAmount(BigDecimal amount);

	void decreaseAmount(BigDecimal amount);

	int manualAdjust(StakeHostingUserAmountAdjustReq req);

	int updateRemark(StakeHostingUserAmountAdjustReq req);
}
