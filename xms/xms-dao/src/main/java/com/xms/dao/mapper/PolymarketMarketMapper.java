package com.xms.dao.mapper;

import com.xms.dao.domain.PolymarketMarket;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Polymarket市场聚合Mapper。
 */
public interface PolymarketMarketMapper extends XmsMapper<PolymarketMarket> {

	/**
	 * 查询后台Polymarket市场列表。
	 *
	 * @param polymarketMarket 查询条件
	 * @return 市场聚合列表
	 */
	List<PolymarketMarket> selectPolymarketMarketList(PolymarketMarket polymarketMarket);

	/**
	 * 下单成功后按marketSlug插入或累加市场聚合金额。
	 *
	 * @param market 本次订单对应的市场快照和金额增量
	 * @return 影响行数
	 */
	int upsertOrderAggregate(PolymarketMarket market);

	/**
	 * 将待结算市场抢占为结算中，用于避免Quartz和延迟队列重复结算同一个市场。
	 *
	 * @param marketSlug 市场slug
	 * @return 1表示抢占成功，0表示市场状态已被其他任务改变
	 */
	int markSettling(@Param("marketSlug") String marketSlug);
}
