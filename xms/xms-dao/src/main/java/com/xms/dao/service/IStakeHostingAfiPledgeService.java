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

	/**
	 * 按用户选择的 AFI 加速配置提交质押。
	 *
	 * @param userId 用户ID
	 * @param stakeHostingOrderId 托管订单ID
	 * @param afiAccelerateConfigId AFI加速配置ID
	 * @param afiPrice 当前AFI价格，单位USDT
	 * @return AFI质押记录
	 */
	StakeHostingAfiPledge pledgeAfi(Long userId, Long stakeHostingOrderId, Long afiAccelerateConfigId, BigDecimal afiPrice);

	int returnPledgeByOrderId(Long stakeHostingOrderId);
}
