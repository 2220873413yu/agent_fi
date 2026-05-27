package com.xms.dao.service;

/**
 * 示例质押订单异步消费Service接口。
 */
public interface IDemoPledgeOrderConsumerService {
	/**
	 * 消费已支付订单并更新用户基础业绩。
	 *
	 * @param orderId 示例质押订单ID
	 */
	void processPaidOrder(Long orderId);
}
