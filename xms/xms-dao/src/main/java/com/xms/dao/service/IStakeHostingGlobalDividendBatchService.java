package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingGlobalDividendBatch;

/**
 * 托管全球分红批次Service接口
 *
 * @author xms
 */
public interface IStakeHostingGlobalDividendBatchService extends XmsDataService<StakeHostingGlobalDividendBatch> {
	java.util.List<StakeHostingGlobalDividendBatch> selectStakeHostingGlobalDividendBatchList(StakeHostingGlobalDividendBatch batch);
}
