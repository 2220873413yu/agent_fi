package com.xms.dao.service;

import com.xms.dao.domain.PolymarketOrder;

import java.util.List;

/**
 * Polymarket平台内部订单DAO服务。
 */
public interface IPolymarketOrderService extends XmsDataService<PolymarketOrder> {

	/**
	 * 按后台筛选条件查询Polymarket内部订单。
	 *
	 * @param polymarketOrder 查询条件
	 * @return 符合条件的订单列表
	 */
	List<PolymarketOrder> selectPolymarketOrderList(PolymarketOrder polymarketOrder);

	/**
	 * 结算已到市场结束时间的待结算订单。
	 *
	 * @param limit 本次最多处理的订单数
	 * @return 被更新的订单数量
	 */
	int settlePendingOrders(Integer limit);
}
