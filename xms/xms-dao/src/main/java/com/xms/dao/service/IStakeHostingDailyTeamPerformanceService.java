package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingDailyTeamPerformance;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管G7每日团队TVL与收益率快照Service接口
 *
 * @author xms
 */
public interface IStakeHostingDailyTeamPerformanceService extends XmsDataService<StakeHostingDailyTeamPerformance> {
	/**
	 * 记录托管订单生效后对所有上级产生的当天团队新增USDT。
	 *
	 * @param orderId 托管订单ID
	 */
	void recordOrderTeamNewAmount(Long orderId);

	/**
	 * 记录托管订单完成后对所有上级产生的次日团队到期USDT。
	 *
	 * @param orderId 托管订单ID
	 * @param rewardDay 当前101收益日，格式yyyyMMdd
	 */
	void recordOrderTeamExpiredAmountNextDay(Long orderId, Integer rewardDay);

	/**
	 * 为101收益日生成或补齐G7团队TVL与收益率快照。
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
	 * 判断用户某天是否存在有效伞下团队TVL。
	 *
	 * @param userId 用户ID
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return true表示有团队TVL
	 */
	boolean hasTeamTvl(Long userId, Integer rewardDay);
}
