package com.xms.common.notify;

/**
 * Polymarket市场结算延迟队列派发服务。
 */
public interface AsyncPolymarketMarketSettleService {

	/**
	 * 投递Polymarket市场结算消息。
	 *
	 * @param marketSlug Polymarket市场slug，用作后续消费者处理的业务主键
	 */
	void sendMarketSettleMessage(String marketSlug);
}
