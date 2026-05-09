package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingGlobalDividendBatch;

/**
 * 托管全球分红批次Mapper接口
 *
 * @author xms
 */
public interface StakeHostingGlobalDividendBatchMapper extends XmsMapper<StakeHostingGlobalDividendBatch> {
	java.util.List<StakeHostingGlobalDividendBatch> selectStakeHostingGlobalDividendBatchList(StakeHostingGlobalDividendBatch batch);
}
