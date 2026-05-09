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
								@Param("selfAmount") BigDecimal selfAmount,
								@Param("teamAmount") BigDecimal teamAmount);

	List<StakeHostingWeeklyCommunityPerformance> selectByUserIdsAndWeek(@Param("userIds") List<Long> userIds,
																		@Param("weekStartTime") Long weekStartTime);
}
