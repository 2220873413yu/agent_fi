package com.xms.dao.mapper;

import com.xms.dao.domain.PolymarketOrder;

import java.util.List;

/**
 * Polymarket内部订单Mapper。
 */
public interface PolymarketOrderMapper extends XmsMapper<PolymarketOrder> {

	/**
	 * 查询后台和App侧使用的Polymarket内部订单列表。
	 *
	 * @param polymarketOrder 查询条件
	 * @return 按ID倒序返回的订单列表
	 */
	List<PolymarketOrder> selectPolymarketOrderList(PolymarketOrder polymarketOrder);
}
