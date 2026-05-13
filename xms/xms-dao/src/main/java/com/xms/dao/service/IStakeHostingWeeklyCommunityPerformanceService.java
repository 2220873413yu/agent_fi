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

	/**
	 * 处理托管订单生效时的周新增小区业绩重算。
	 *
	 * @param orderId 托管订单ID
	 */
	void processOrderWeeklyPerformance(Long orderId);

	/**
	 * 处理托管订单到期完成时的周小区业绩重算。
	 *
	 * @param orderId 托管订单ID
	 */
	void processOrderWeeklyExpirePerformance(Long orderId);
}
