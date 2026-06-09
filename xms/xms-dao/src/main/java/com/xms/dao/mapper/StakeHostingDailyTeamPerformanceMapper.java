package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingDailyTeamPerformance;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

/**
 * 托管G7每日团队业绩与收益率快照Mapper接口
 *
 * @author xms
 */
public interface StakeHostingDailyTeamPerformanceMapper extends XmsMapper<StakeHostingDailyTeamPerformance> {
	/**
	 * 查询托管G7每日团队业绩与静态收益率快照列表。
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
	 * @deprecated G7静态日利率已改为团队总业绩口径，到期审计字段不再参与G7公式。
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
											@Param("beginStatDay") Integer beginStatDay);

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
																	   @Param("beginStatDay") Integer beginStatDay);

	/**
	 * 批量查询多个用户某一天的G7快照。
	 *
	 * <p>用于G7快照计算昨日团队总业绩和昨日新增审计值，避免在用户循环中逐个查昨日记录。</p>
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
	 * 查询当前团队总业绩大于0的用户ID。
	 *
	 * <p>G7总业绩TVL是存量状态指标。即使当天没有购买事件，只要用户当前仍有团队总业绩，
	 * 也需要生成当天G7快照，避免快照断档。</p>
	 *
	 * @return 用户ID列表
	 */
	List<Long> selectUserIdsWithUmbrellaPerformance();

	/**
	 * 查询指定日期已计算快照中团队总业绩大于0的用户ID。
	 *
	 * <p>用于捕捉今天团队总业绩归零或下降的负增长场景，避免只查当前umbrella_performance时漏算。</p>
	 *
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @return 用户ID列表
	 */
	List<Long> selectUserIdsWithYesterdayTeamTotalPerformance(@Param("statDay") Integer statDay);

	/**
	 * 查询最近一段自然日内发生过真实G7事件的用户ID。
	 *
	 * <p>真实事件指团队总业绩发生变化，或存在团队新增/到期审计金额。该查询用于团队总业绩归零后继续生成
	 * 最多7天平滑窗口快照，避免仅查询任意历史快照导致快照自己续自己。</p>
	 *
	 * @param startDay 起始统计日期，包含，格式yyyyMMdd
	 * @param endDay 结束统计日期，不包含，格式yyyyMMdd
	 * @return 用户ID列表
	 */
	List<Long> selectUserIdsWithRecentG7Event(@Param("startDay") Integer startDay,
											  @Param("endDay") Integer endDay);

	/**
	 * 查询某个快照时间点用户伞下有效托管USDT TVL。
	 *
	 * @deprecated G7静态日利率已改为直接读取 t_user_info.umbrella_performance，不再回查订单汇总。
	 *
	 * @param userId 用户ID
	 * @param snapshotTime 快照时间，格式yyyyMMddHHmmss
	 * @return 伞下有效托管USDT金额
	 */
	@Deprecated
	BigDecimal selectTeamTvlAt(@Param("userId") Long userId,
							   @Param("snapshotTime") Long snapshotTime);
}
