package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingRewardSettlement;

import java.util.List;

/**
 * 托管奖励结算明细Mapper接口
 *
 * @author xms
 */
public interface StakeHostingRewardSettlementMapper extends XmsMapper<StakeHostingRewardSettlement> {
	List<StakeHostingRewardSettlement> selectStakeHostingRewardSettlementList(StakeHostingRewardSettlement settlement);
}
