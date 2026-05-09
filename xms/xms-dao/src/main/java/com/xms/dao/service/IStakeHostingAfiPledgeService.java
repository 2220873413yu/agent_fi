package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingAfiPledge;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管订单AFI质押记录Service接口
 *
 * @author xms
 */
public interface IStakeHostingAfiPledgeService extends XmsDataService<StakeHostingAfiPledge> {
	List<StakeHostingAfiPledge> selectStakeHostingAfiPledgeList(StakeHostingAfiPledge pledge);

	StakeHostingAfiPledge pledgeAfi(Long userId, Long stakeHostingOrderId, BigDecimal afiAmount);

	int returnPledgeByOrderId(Long stakeHostingOrderId);
}
