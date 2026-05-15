package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管全球分红权重快照Mapper接口
 *
 * @author xms
 */
public interface StakeHostingGlobalDividendWeightSnapshotMapper extends XmsMapper<StakeHostingGlobalDividendWeightSnapshot> {
	List<StakeHostingGlobalDividendWeightSnapshot> selectStakeHostingGlobalDividendWeightSnapshotList(StakeHostingGlobalDividendWeightSnapshot snapshot);

	int upsertWeightSnapshot(@Param("userId") Long userId,
							 @Param("account") String account,
							 @Param("weekStartTime") Long weekStartTime,
							 @Param("weekEndTime") Long weekEndTime,
							 @Param("totalLineWeight") BigDecimal totalLineWeight,
							 @Param("maxLineWeight") BigDecimal maxLineWeight,
							 @Param("communityWeight") BigDecimal communityWeight,
							 @Param("previousCommunityWeight") BigDecimal previousCommunityWeight,
							 @Param("dividendWeight") BigDecimal dividendWeight);

	BigDecimal selectLineValidWeight(@Param("directUserId") Long directUserId,
									 @Param("snapshotTime") Long snapshotTime);

	BigDecimal selectPreviousCommunityWeight(@Param("userId") Long userId,
											 @Param("weekStartTime") Long weekStartTime);
}
