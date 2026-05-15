package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Mapper for stake hosting global dividend weekly weight snapshots.
 */
public interface StakeHostingGlobalDividendWeightSnapshotMapper extends XmsMapper<StakeHostingGlobalDividendWeightSnapshot> {
	List<StakeHostingGlobalDividendWeightSnapshot> selectStakeHostingGlobalDividendWeightSnapshotList(StakeHostingGlobalDividendWeightSnapshot snapshot);

	List<StakeHostingGlobalDividendWeightSnapshot> selectLatestBeforeWeek(@Param("weekStartTime") Long weekStartTime);

	int batchUpsert(@Param("snapshots") List<StakeHostingGlobalDividendWeightSnapshot> snapshots);
}
