package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingRewardSettlement;

import java.util.List;

/**
 * 托管奖励结算明细Service接口
 *
 * @author xms
 */
public interface IStakeHostingRewardSettlementService extends XmsDataService<StakeHostingRewardSettlement> {
	List<StakeHostingRewardSettlement> selectStakeHostingRewardSettlementList(StakeHostingRewardSettlement settlement);
}
