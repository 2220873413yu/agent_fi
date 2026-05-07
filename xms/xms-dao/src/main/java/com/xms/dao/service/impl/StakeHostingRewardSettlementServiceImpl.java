package com.xms.dao.service.impl;

import com.xms.dao.domain.StakeHostingRewardSettlement;
import com.xms.dao.mapper.StakeHostingRewardSettlementMapper;
import com.xms.dao.service.IStakeHostingRewardSettlementService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 托管奖励结算明细Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingRewardSettlementServiceImpl extends XmsDataServiceImpl<StakeHostingRewardSettlementMapper, StakeHostingRewardSettlement> implements IStakeHostingRewardSettlementService {
	@Override
	public List<StakeHostingRewardSettlement> selectStakeHostingRewardSettlementList(StakeHostingRewardSettlement settlement) {
		return baseMapper.selectStakeHostingRewardSettlementList(settlement);
	}
}
