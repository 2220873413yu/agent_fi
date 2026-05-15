package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;

import java.util.List;

/**
 * Service for stake hosting global dividend weekly weight snapshots.
 */
public interface IStakeHostingGlobalDividendWeightSnapshotService extends XmsDataService<StakeHostingGlobalDividendWeightSnapshot> {
	List<StakeHostingGlobalDividendWeightSnapshot> selectStakeHostingGlobalDividendWeightSnapshotList(StakeHostingGlobalDividendWeightSnapshot snapshot);

	List<StakeHostingGlobalDividendWeightSnapshot> selectLatestBeforeWeek(Long weekStartTime);

	void batchUpsert(List<StakeHostingGlobalDividendWeightSnapshot> snapshots);
}
