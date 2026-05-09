package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingGlobalDividendPoolLog;

import java.util.List;

/**
 * 托管全球分红奖池流水Mapper接口
 *
 * @author xms
 */
public interface StakeHostingGlobalDividendPoolLogMapper extends XmsMapper<StakeHostingGlobalDividendPoolLog> {
	List<StakeHostingGlobalDividendPoolLog> selectStakeHostingGlobalDividendPoolLogList(StakeHostingGlobalDividendPoolLog log);
}
