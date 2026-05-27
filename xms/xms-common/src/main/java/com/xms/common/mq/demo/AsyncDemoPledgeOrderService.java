package com.xms.common.mq.demo;

/**
 * 示例质押订单后置处理消息生产者。
 */
public interface AsyncDemoPledgeOrderService {
	/**
	 * 投递示例质押订单后置处理消息。
	 *
	 * @param orderId 示例质押订单ID
	 */
	void sendMessage(Long orderId);
}
