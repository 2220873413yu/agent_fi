package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingDailyTeamPerformance;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 托管G7每日团队新增业绩与收益率快照Mapper接口
 *
 * @author xms
 */
public interface StakeHostingDailyTeamPerformanceMapper extends XmsMapper<StakeHostingDailyTeamPerformance> {
	/**
	 * 查询托管G7每日团队新增业绩与静态收益率快照列表。
	 *
	 * @param performance 查询条件
	 * @return G7每日快照列表
	 */
	List<StakeHostingDailyTeamPerformance> selectStakeHostingDailyTeamPerformanceList(StakeHostingDailyTeamPerformance performance);

	/**
	 * 累加用户某天的伞下团队新增托管USDT金额。
	 *
	 * @param userId 上级用户ID
	 * @param account 上级钱包地址快照
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @param amount 新增托管USDT金额
	 * @return 影响行数
	 */
	int upsertTeamNewAmount(@Param("userId") Long userId,
							@Param("account") String account,
							@Param("statDay") Integer statDay,
							@Param("amount") BigDecimal amount);

	/**
	 * 累加用户某天的伞下团队到期托管USDT金额。
	 *
	 * @deprecated G7静态日利率已改为每日新增对比口径，订单到期金额不再参与G7。
	 *
	 * @param userId 上级用户ID
	 * @param account 上级钱包地址快照
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @param amount 到期托管USDT金额
	 * @return 影响行数
	 */
	@Deprecated
	int upsertTeamExpiredAmount(@Param("userId") Long userId,
								@Param("account") String account,
								@Param("statDay") Integer statDay,
								@Param("amount") BigDecimal amount);

	/**
	 * 插入或保留用户某天的G7日汇总空记录。
	 *
	 * @param userId 用户ID
	 * @param account 钱包地址快照
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @return 影响行数
	 */
	int upsertEmptyDay(@Param("userId") Long userId,
					   @Param("account") String account,
					   @Param("statDay") Integer statDay);

	/**
	 * 查询用户指定日期之前最近一次已计算的G7快照。
	 *
	 * @param userId 用户ID
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @return 最近一次快照
	 */
	StakeHostingDailyTeamPerformance selectLatestBefore(@Param("userId") Long userId,
														 @Param("statDay") Integer statDay);

	/**
	 * 查询用户指定日期之前最近最多6天的G_day。
	 *
	 * @param userId 用户ID
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @return G_day列表，按日期倒序
	 */
	List<BigDecimal> selectRecentGDayBefore(@Param("userId") Long userId,
											@Param("statDay") Integer statDay,
											@Param("beginStatDay") Integer beginStatDay,
											@Param("rateSource") Integer rateSource);

	/**
	 * 批量查询多个用户在指定日期之前最近最多6天的G_day。
	 *
	 * <p>用于101收益任务准备G7快照时一次性预加载历史G值，避免按用户逐个查询造成N+1。</p>
	 *
	 * @param userIds 用户ID集合
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @return G7快照列表，每个用户最多6条，按用户和日期倒序返回
	 */
	List<StakeHostingDailyTeamPerformance> selectRecentGDayBeforeBatch(@Param("userIds") Collection<Long> userIds,
																	   @Param("statDay") Integer statDay,
																	   @Param("beginStatDay") Integer beginStatDay,
																	   @Param("rateSource") Integer rateSource);

	/**
	 * 批量查询多个用户某一天的团队新增业绩。
	 *
	 * <p>用于G7快照计算昨日新增业绩，避免在用户循环中逐个查昨日记录。</p>
	 *
	 * @param userIds 用户ID集合
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @return 指定日期的G7快照列表
	 */
	List<StakeHostingDailyTeamPerformance> selectByUserIdsAndStatDay(@Param("userIds") Collection<Long> userIds,
																	  @Param("statDay") Integer statDay);

	/**
	 * 查询某天已有G7日汇总记录的用户ID。
	 *
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @return 用户ID列表
	 */
	List<Long> selectUserIdsByStatDay(@Param("statDay") Integer statDay);

	/**
	 * 查询某个快照时间点用户伞下有效托管USDT TVL。
	 *
	 * @deprecated G7静态日利率已改为每日新增对比口径，不再回查有效托管余额。
	 *
	 * @param userId 用户ID
	 * @param snapshotTime 快照时间，格式yyyyMMddHHmmss
	 * @return 伞下有效托管USDT金额
	 */
	@Deprecated
	BigDecimal selectTeamTvlAt(@Param("userId") Long userId,
							   @Param("snapshotTime") Long snapshotTime);
}
