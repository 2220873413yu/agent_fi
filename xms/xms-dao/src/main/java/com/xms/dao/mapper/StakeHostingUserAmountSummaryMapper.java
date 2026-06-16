package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingUserAmountSummary;

import java.util.List;

/**
 * 托管用户累计金额Mapper。
 */
public interface StakeHostingUserAmountSummaryMapper extends XmsMapper<StakeHostingUserAmountSummary> {
	List<StakeHostingUserAmountSummary> selectStakeHostingUserAmountSummaryList(StakeHostingUserAmountSummary summary);
}
