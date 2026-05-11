package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingUserRewardSummary;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管用户奖励累计汇总Service接口
 *
 * @author xms
 */
public interface IStakeHostingUserRewardSummaryService extends XmsDataService<StakeHostingUserRewardSummary> {
	/**
	 * 按用户ID查询托管奖励累计汇总。
	 *
	 * @param userId 用户ID
	 * @return 托管奖励累计汇总，未初始化时返回null
	 */
	StakeHostingUserRewardSummary getByUserId(Long userId);

	/**
	 * 初始化用户托管奖励累计汇总。
	 *
	 * @param userId 用户ID
	 */
	void initUser(Long userId);

	/**
	 * 累加用户托管极差奖累计金额。
	 *
	 * @param userId 用户ID
	 * @param amount 本次极差奖金额，单位USDT
	 */
	void addDiffReward(Long userId, BigDecimal amount);

	/**
	 * 累加用户托管平级奖累计金额。
	 *
	 * @param userId 用户ID
	 * @param amount 本次平级奖金额，单位USDT
	 */
	void addSameLevelReward(Long userId, BigDecimal amount);

	/**
	 * 累加用户托管全球分红累计金额。
	 *
	 * @param userId 用户ID
	 * @param amount 本次全球分红金额，单位USDT
	 */
	void addGlobalDividend(Long userId, BigDecimal amount);

	/**
	 * 批量累加用户托管团队收益汇总。
	 *
	 * @param list 用户团队收益增量列表，金额单位USDT
	 */
	void batchAddTeamRewardSummary(List<StakeHostingUserRewardSummary> list);
}
