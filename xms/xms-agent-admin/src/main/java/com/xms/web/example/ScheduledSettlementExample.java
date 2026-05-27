package com.xms.web.example;

import lombok.extern.slf4j.Slf4j;

/**
 * 后台定时任务触发结算示例。
 *
 * <p>真实 Quartz 入口通常放在 {@code XmsTask} 中。本示例只展示推荐职责边界：
 * 定时任务负责扫描和派发，复杂结算、奖励计算和钱包发放放到 Service 中处理。</p>
 */
@Slf4j
public class ScheduledSettlementExample {

	/**
	 * 定时扫描到期待结算数据，并派发给后续统一结算逻辑。
	 *
	 * @param settlementDispatcher 结算派发服务
	 * @param limit 本次最多派发数量，避免单次任务执行过久
	 * @return 成功派发数量
	 */
	public int dispatchExpiredOrders(SettlementDispatcher settlementDispatcher, int limit) {
		log.info("dispatch expired settlement orders, limit={}", limit);
		int count = settlementDispatcher.dispatchExpired(limit);
		log.info("dispatched expired settlement orders, count={}", count);
		return count;
	}

	/**
	 * 重投递长时间卡在处理中的结算数据。
	 *
	 * @param settlementDispatcher 结算派发服务
	 * @param limit 本次最多重投递数量
	 * @return 成功重投递数量
	 */
	public int redispatchStuckOrders(SettlementDispatcher settlementDispatcher, int limit) {
		log.info("redispatch stuck settlement orders, limit={}", limit);
		int count = settlementDispatcher.redispatchStuck(limit);
		log.info("redispatched stuck settlement orders, count={}", count);
		return count;
	}

	/**
	 * 示例结算派发接口，真实业务可对应订单、市场、释放记录或奖励结算服务。
	 */
	public interface SettlementDispatcher {
		int dispatchExpired(int limit);

		int redispatchStuck(int limit);
	}
}
