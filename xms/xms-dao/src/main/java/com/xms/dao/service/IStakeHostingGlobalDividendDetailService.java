package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingGlobalDividendDetail;

/**
 * 托管全球分红明细Service接口
 *
 * @author xms
 */
public interface IStakeHostingGlobalDividendDetailService extends XmsDataService<StakeHostingGlobalDividendDetail> {
	java.util.List<StakeHostingGlobalDividendDetail> selectStakeHostingGlobalDividendDetailList(StakeHostingGlobalDividendDetail detail);
}
