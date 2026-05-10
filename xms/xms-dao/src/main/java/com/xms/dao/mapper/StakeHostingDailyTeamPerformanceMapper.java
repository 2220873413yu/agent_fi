package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingDailyTeamPerformance;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管G7每日团队TVL与收益率快照Mapper接口
 *
 * @author xms
 */
public interface StakeHostingDailyTeamPerformanceMapper extends XmsMapper<StakeHostingDailyTeamPerformance> {
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
	 * @param userId 上级用户ID
	 * @param account 上级钱包地址快照
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @param amount 到期托管USDT金额
	 * @return 影响行数
	 */
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
	 * @param userId 用户ID
	 * @param snapshotTime 快照时间，格式yyyyMMddHHmmss
	 * @return 伞下有效托管USDT金额
	 */
	BigDecimal selectTeamTvlAt(@Param("userId") Long userId,
							   @Param("snapshotTime") Long snapshotTime);
}
