package com.xms.dao.service.impl;

import com.xms.dao.domain.StakeHostingGlobalDividendDetail;
import com.xms.dao.mapper.StakeHostingGlobalDividendDetailMapper;
import com.xms.dao.service.IStakeHostingGlobalDividendDetailService;
import org.springframework.stereotype.Service;

/**
 * 托管全球分红明细Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingGlobalDividendDetailServiceImpl extends XmsDataServiceImpl<StakeHostingGlobalDividendDetailMapper, StakeHostingGlobalDividendDetail> implements IStakeHostingGlobalDividendDetailService {
	@Override
	public java.util.List<StakeHostingGlobalDividendDetail> selectStakeHostingGlobalDividendDetailList(StakeHostingGlobalDividendDetail detail) {
		return baseMapper.selectStakeHostingGlobalDividendDetailList(detail);
	}
}
