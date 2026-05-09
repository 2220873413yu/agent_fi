package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingGlobalDividendPool;

import java.math.BigDecimal;

/**
 * 托管全球分红奖池Service接口
 *
 * @author xms
 */
public interface IStakeHostingGlobalDividendPoolService extends XmsDataService<StakeHostingGlobalDividendPool> {
	StakeHostingGlobalDividendPool getOrInitPool();

	StakeHostingGlobalDividendPool adjustPool(Integer flowType, BigDecimal amount, String remark, String operator);

	StakeHostingGlobalDividendPool incomeDailyServiceFee(Integer settlementDay, BigDecimal amount, String operator);

	StakeHostingGlobalDividendPool expenseWeeklyDividend(String batchNo, BigDecimal amount, String operator);
}
