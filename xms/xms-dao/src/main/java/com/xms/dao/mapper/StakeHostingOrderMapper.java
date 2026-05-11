package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.entity.dto.StakeHostingOrderListDto;

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

	/**
	 * 查询后台托管订单列表展示数据。
	 *
	 * @param query 查询条件
	 * @return 托管订单列表，包含AFI质押比例和加速倍率展示字段
	 */
	List<StakeHostingOrderListDto> selectStakeHostingOrderDtoList(StakeHostingOrderListDto query);
}
