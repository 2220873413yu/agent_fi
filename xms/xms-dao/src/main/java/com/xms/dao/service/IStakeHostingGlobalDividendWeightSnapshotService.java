package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;

import java.util.List;

/**
 * 托管全球分红权重快照Service接口
 *
 * @author xms
 */
public interface IStakeHostingGlobalDividendWeightSnapshotService extends XmsDataService<StakeHostingGlobalDividendWeightSnapshot> {
	List<StakeHostingGlobalDividendWeightSnapshot> selectStakeHostingGlobalDividendWeightSnapshotList(StakeHostingGlobalDividendWeightSnapshot snapshot);

	/**
	 * Rebuilds the global dividend weight snapshots for one natural week.
	 *
	 * @param weekStartTime week start time in yyyyMMddHHmmss format
	 * @param weekEndTime week end time in yyyyMMddHHmmss format; this is also the effective order snapshot time
	 */
	void prepareWeeklySnapshots(Long weekStartTime, Long weekEndTime);
}
