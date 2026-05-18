package com.xms.dao.service.impl;

import com.xms.dao.domain.PolymarketMarket;
import com.xms.dao.mapper.PolymarketMarketMapper;
import com.xms.dao.service.IPolymarketMarketService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Polymarket市场聚合服务实现。
 *
 * <p>该服务负责后台列表、下单时市场金额累加，以及结算任务抢占市场状态。</p>
 */
@Service
public class PolymarketMarketServiceImpl extends XmsDataServiceImpl<PolymarketMarketMapper, PolymarketMarket>
	implements IPolymarketMarketService {

	/**
	 * 查询后台Polymarket市场聚合列表。
	 *
	 * @param polymarketMarket 查询条件
	 * @return 市场聚合列表
	 */
	@Override
	public List<PolymarketMarket> selectPolymarketMarketList(PolymarketMarket polymarketMarket) {
		return baseMapper.selectPolymarketMarketList(polymarketMarket);
	}

	/**
	 * 下单成功后按marketSlug插入或累加市场聚合金额。
	 *
	 * @param market 市场快照和本单金额增量
	 * @return true表示写入成功
	 */
	@Override
	public boolean upsertOrderAggregate(PolymarketMarket market) {
		return baseMapper.upsertOrderAggregate(market) > 0;
	}

	/**
	 * 抢占待结算市场为结算中。
	 *
	 * @param marketSlug 市场slug
	 * @return true表示抢占成功
	 */
	@Override
	public boolean markSettling(String marketSlug) {
		return baseMapper.markSettling(marketSlug) == 1;
	}
}
