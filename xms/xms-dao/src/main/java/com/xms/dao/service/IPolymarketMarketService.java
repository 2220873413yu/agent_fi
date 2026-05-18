package com.xms.dao.service;

import com.xms.dao.domain.PolymarketMarket;

import java.util.List;

/**
 * Polymarket市场聚合服务。
 */
public interface IPolymarketMarketService extends XmsDataService<PolymarketMarket> {

	/**
	 * 查询后台Polymarket市场聚合列表。
	 *
	 * @param polymarketMarket 查询条件
	 * @return 市场聚合列表
	 */
	List<PolymarketMarket> selectPolymarketMarketList(PolymarketMarket polymarketMarket);

	/**
	 * 下单成功后插入或累加市场聚合金额。
	 *
	 * @param market 市场快照和本单金额增量
	 * @return 是否写入成功
	 */
	boolean upsertOrderAggregate(PolymarketMarket market);

	/**
	 * 抢占一个待结算市场为结算中。
	 *
	 * @param marketSlug 市场slug
	 * @return true表示当前线程获得结算权
	 */
	boolean markSettling(String marketSlug);
}
