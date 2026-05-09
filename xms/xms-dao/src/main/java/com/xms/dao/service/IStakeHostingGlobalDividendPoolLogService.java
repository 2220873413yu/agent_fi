package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingGlobalDividendPoolLog;

import java.util.List;

/**
 * 托管全球分红奖池流水Service接口
 *
 * @author xms
 */
public interface IStakeHostingGlobalDividendPoolLogService extends XmsDataService<StakeHostingGlobalDividendPoolLog> {
	List<StakeHostingGlobalDividendPoolLog> selectStakeHostingGlobalDividendPoolLogList(StakeHostingGlobalDividendPoolLog log);
}
