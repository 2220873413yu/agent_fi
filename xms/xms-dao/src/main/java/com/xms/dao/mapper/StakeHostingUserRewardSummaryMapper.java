package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingUserRewardSummary;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管用户奖励累计汇总Mapper接口
 *
 * @author xms
 */
public interface StakeHostingUserRewardSummaryMapper extends XmsMapper<StakeHostingUserRewardSummary> {
	/**
	 * 按用户ID查询托管奖励累计汇总。
	 *
	 * @param userId 用户ID
	 * @return 托管奖励累计汇总，未初始化时返回null
	 */
	StakeHostingUserRewardSummary selectByUserId(@Param("userId") Long userId);

	/**
	 * 初始化用户托管奖励累计汇总，已存在时忽略。
	 *
	 * @param userId 用户ID
	 * @return 影响行数
	 */
	int initUser(@Param("userId") Long userId);

	/**
	 * 累加托管极差奖累计金额。
	 *
	 * @param userId 用户ID
	 * @param amount 本次极差奖金额，单位USDT
	 * @return 影响行数
	 */
	int addDiffReward(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

	/**
	 * 累加托管平级奖累计金额。
	 *
	 * @param userId 用户ID
	 * @param amount 本次平级奖金额，单位USDT
	 * @return 影响行数
	 */
	int addSameLevelReward(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

	/**
	 * 累加托管全球分红累计金额。
	 *
	 * @param userId 用户ID
	 * @param amount 本次全球分红金额，单位USDT
	 * @return 影响行数
	 */
	int addGlobalDividend(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

	/**
	 * 批量累加托管团队收益汇总。
	 *
	 * @param list 用户团队收益增量列表，包含极差奖和平级奖累计增量
	 * @return 影响行数
	 */
	int batchAddTeamRewardSummary(@Param("list") List<StakeHostingUserRewardSummary> list);
}
