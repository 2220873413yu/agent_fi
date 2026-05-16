package com.xms.dao.mapper;

import com.xms.dao.domain.PolymarketOrder;

import java.util.List;

/**
 * Polymarket internal order mapper.
 */
public interface PolymarketOrderMapper extends XmsMapper<PolymarketOrder> {

	/**
	 * Selects Polymarket internal orders for admin and app list pages.
	 *
	 * @param polymarketOrder query filter
	 * @return matching orders ordered by id descending
	 */
	List<PolymarketOrder> selectPolymarketOrderList(PolymarketOrder polymarketOrder);
}
