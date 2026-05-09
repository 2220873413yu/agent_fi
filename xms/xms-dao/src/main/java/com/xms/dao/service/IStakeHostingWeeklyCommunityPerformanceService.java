package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingWeeklyCommunityPerformance;

import java.util.List;

/**
 * 托管每周新增小区业绩Service接口
 *
 * @author xms
 */
public interface IStakeHostingWeeklyCommunityPerformanceService extends XmsDataService<StakeHostingWeeklyCommunityPerformance> {
	List<StakeHostingWeeklyCommunityPerformance> selectStakeHostingWeeklyCommunityPerformanceList(StakeHostingWeeklyCommunityPerformance performance);

	void processOrderWeeklyPerformance(Long orderId);
}
