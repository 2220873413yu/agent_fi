package com.xms.dao.service.impl;

import com.xms.dao.domain.StakeHostingGlobalDividendBatch;
import com.xms.dao.mapper.StakeHostingGlobalDividendBatchMapper;
import com.xms.dao.service.IStakeHostingGlobalDividendBatchService;
import org.springframework.stereotype.Service;

/**
 * 托管全球分红批次Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingGlobalDividendBatchServiceImpl extends XmsDataServiceImpl<StakeHostingGlobalDividendBatchMapper, StakeHostingGlobalDividendBatch> implements IStakeHostingGlobalDividendBatchService {
	@Override
	public java.util.List<StakeHostingGlobalDividendBatch> selectStakeHostingGlobalDividendBatchList(StakeHostingGlobalDividendBatch batch) {
		return baseMapper.selectStakeHostingGlobalDividendBatchList(batch);
	}
}
