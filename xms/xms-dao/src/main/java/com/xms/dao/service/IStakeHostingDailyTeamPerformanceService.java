package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingDailyTeamPerformance;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管G7每日团队新增业绩与收益率快照Service接口
 *
 * @author xms
 */
public interface IStakeHostingDailyTeamPerformanceService extends XmsDataService<StakeHostingDailyTeamPerformance> {
	/**
	 * 查询托管G7每日团队新增业绩与静态收益率快照列表。
	 *
	 * @param performance 查询条件
	 * @return G7每日快照列表
	 */
	List<StakeHostingDailyTeamPerformance> selectStakeHostingDailyTeamPerformanceList(StakeHostingDailyTeamPerformance performance);

	/**
	 * 记录托管订单生效后对所有上级产生的当天团队新增USDT。
	 *
	 * @param orderId 托管订单ID
	 */
	void recordOrderTeamNewAmount(Long orderId);

	/**
	 * 记录托管订单完成后对所有上级产生的次日团队到期USDT。
	 *
	 * @deprecated G7静态日利率已改为每日新增对比口径，订单到期不再参与G7计算。
	 *
	 * @param orderId 托管订单ID
	 * @param rewardDay 当前101收益日，格式yyyyMMdd
	 */
	@Deprecated
	void recordOrderTeamExpiredAmountNextDay(Long orderId, Integer rewardDay);

	/**
	 * 为101收益日生成或补齐G7团队新增业绩与收益率快照。
	 *
	 * @param rewardDay 当前101收益日，格式yyyyMMdd
	 * @param rewardUserIds 当天存在产出中托管订单的用户ID
	 */
	void prepareDailySnapshots(Integer rewardDay, List<Long> rewardUserIds);

	/**
	 * 获取用户某天已计算的G7快照。
	 *
	 * @param userId 用户ID
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return G7快照
	 */
	StakeHostingDailyTeamPerformance getCalculatedSnapshot(Long userId, Integer rewardDay);

	/**
	 * 判断用户某天是否存在可用于G7计算的团队新增业绩。
	 *
	 * @param userId 用户ID
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return true表示昨日或今日团队新增大于0
	 */
	boolean hasTeamTvl(Long userId, Integer rewardDay);
}
