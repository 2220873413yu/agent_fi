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
	 * 派发已到期的Polymarket待结算市场。
	 *
	 * <p>该方法只把市场从待结算改为结算中，并预留延迟队列发送位置；不查询Polymarket、不处理订单、不写钱包。</p>
	 *
	 * @param limit 本批最多派发的市场数量
	 * @return 成功改为结算中的市场数量
	 */
	int settlePendingOrders(Integer limit);

	/**
	 * 派发或处理单个市场。
	 *
	 * @param marketSlug Polymarket市场slug
	 * @return true表示市场状态被当前调用更新
	 */
	boolean settleMarketBySlug(String marketSlug);

	/**
	 * 处理已经处于结算中的市场。
	 *
	 * <p>该方法才真正查询Polymarket、批量结算订单和写钱包。后续延迟队列消费者应调用它。</p>
	 *
	 * @param marketSlug Polymarket市场slug
	 * @return true表示市场状态被当前调用更新
	 */
	boolean processSettlingMarket(String marketSlug);
}
