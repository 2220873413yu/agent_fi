package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingOrder;

import java.util.List;

/**
 * 托管订单Mapper接口
 *
 * @author xms
 */
public interface StakeHostingOrderMapper extends XmsMapper<StakeHostingOrder> {
	/**
	 * 查询托管订单列表
	 *
	 * @param stakeHostingOrder 托管订单
	 * @return 托管订单集合
	 */
	List<StakeHostingOrder> selectStakeHostingOrderList(StakeHostingOrder stakeHostingOrder);
}
