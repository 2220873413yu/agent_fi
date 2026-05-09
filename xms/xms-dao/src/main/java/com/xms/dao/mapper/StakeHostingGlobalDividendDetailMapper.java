package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingGlobalDividendDetail;

/**
 * 托管全球分红明细Mapper接口
 *
 * @author xms
 */
public interface StakeHostingGlobalDividendDetailMapper extends XmsMapper<StakeHostingGlobalDividendDetail> {
	java.util.List<StakeHostingGlobalDividendDetail> selectStakeHostingGlobalDividendDetailList(StakeHostingGlobalDividendDetail detail);
}
