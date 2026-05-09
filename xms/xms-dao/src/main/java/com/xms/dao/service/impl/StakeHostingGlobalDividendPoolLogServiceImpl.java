package com.xms.dao.service.impl;

import com.xms.dao.domain.StakeHostingGlobalDividendPoolLog;
import com.xms.dao.mapper.StakeHostingGlobalDividendPoolLogMapper;
import com.xms.dao.service.IStakeHostingGlobalDividendPoolLogService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 托管全球分红奖池流水Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingGlobalDividendPoolLogServiceImpl extends XmsDataServiceImpl<StakeHostingGlobalDividendPoolLogMapper, StakeHostingGlobalDividendPoolLog> implements IStakeHostingGlobalDividendPoolLogService {
	@Override
	public List<StakeHostingGlobalDividendPoolLog> selectStakeHostingGlobalDividendPoolLogList(StakeHostingGlobalDividendPoolLog log) {
		return baseMapper.selectStakeHostingGlobalDividendPoolLogList(log);
	}
}
