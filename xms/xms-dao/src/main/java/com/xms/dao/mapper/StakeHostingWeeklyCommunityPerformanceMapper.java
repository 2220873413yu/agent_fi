package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingWeeklyCommunityPerformance;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管每周新增小区业绩Mapper接口
 *
 * @author xms
 */
public interface StakeHostingWeeklyCommunityPerformanceMapper extends XmsMapper<StakeHostingWeeklyCommunityPerformance> {
	List<StakeHostingWeeklyCommunityPerformance> selectStakeHostingWeeklyCommunityPerformanceList(StakeHostingWeeklyCommunityPerformance performance);

	int upsertWeeklyPerformance(@Param("userId") Long userId,
								@Param("account") String account,
								@Param("weekStartTime") Long weekStartTime,
								@Param("weekEndTime") Long weekEndTime,
								@Param("selfIncreasePoints") BigDecimal selfIncreasePoints,
								@Param("selfExpirePoints") BigDecimal selfExpirePoints,
								@Param("teamIncreasePoints") BigDecimal teamIncreasePoints,
								@Param("teamExpirePoints") BigDecimal teamExpirePoints,
								@Param("selfNetPoints") BigDecimal selfNetPoints,
								@Param("teamNetPoints") BigDecimal teamNetPoints,
								@Param("selfIncreaseAmount") BigDecimal selfIncreaseAmount,
								@Param("selfExpireAmount") BigDecimal selfExpireAmount,
								@Param("teamIncreaseAmount") BigDecimal teamIncreaseAmount,
								@Param("teamExpireAmount") BigDecimal teamExpireAmount,
								@Param("selfNetAmount") BigDecimal selfNetAmount,
								@Param("teamNetAmount") BigDecimal teamNetAmount);

	List<StakeHostingWeeklyCommunityPerformance> selectByUserIdsAndWeek(@Param("userIds") List<Long> userIds,
																		@Param("weekStartTime") Long weekStartTime);

	BigDecimal selectSelfWeeklyIncreasePoints(@Param("userId") Long userId,
											  @Param("weekStartTime") Long weekStartTime,
											  @Param("weekEndTime") Long weekEndTime);

	BigDecimal selectSelfWeeklyExpirePoints(@Param("userId") Long userId,
											@Param("weekStartTime") Long weekStartTime,
											@Param("weekEndTime") Long weekEndTime);

	BigDecimal selectSelfWeeklyIncreaseAmount(@Param("userId") Long userId,
											  @Param("weekStartTime") Long weekStartTime,
											  @Param("weekEndTime") Long weekEndTime);

	BigDecimal selectSelfWeeklyExpireAmount(@Param("userId") Long userId,
											@Param("weekStartTime") Long weekStartTime,
											@Param("weekEndTime") Long weekEndTime);

	BigDecimal selectTeamWeeklyIncreasePoints(@Param("userId") Long userId,
											  @Param("weekStartTime") Long weekStartTime,
											  @Param("weekEndTime") Long weekEndTime);

	BigDecimal selectTeamWeeklyExpirePoints(@Param("userId") Long userId,
											@Param("weekStartTime") Long weekStartTime,
											@Param("weekEndTime") Long weekEndTime);

	BigDecimal selectTeamWeeklyIncreaseAmount(@Param("userId") Long userId,
											  @Param("weekStartTime") Long weekStartTime,
											  @Param("weekEndTime") Long weekEndTime);

	BigDecimal selectTeamWeeklyExpireAmount(@Param("userId") Long userId,
											@Param("weekStartTime") Long weekStartTime,
											@Param("weekEndTime") Long weekEndTime);

	BigDecimal selectLineValidPoints(@Param("directUserId") Long directUserId,
									 @Param("snapshotTime") Long snapshotTime);

	BigDecimal selectLineValidAmount(@Param("directUserId") Long directUserId,
									 @Param("snapshotTime") Long snapshotTime);
}
