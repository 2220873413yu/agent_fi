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

	/**
	 * 按托管订单退还未退还的AFI质押。
	 *
	 * <p>只处理状态为1=生效中的质押记录；退还后状态更新为2=已退还，并写AFI退还钱包流水。</p>
	 *
	 * @param stakeHostingOrderId 托管订单ID
	 * @return 1表示成功退还，0表示无可退还记录
	 */
	int returnPledgeByOrderId(Long stakeHostingOrderId);
}
