package com.xms.common.mq.dynamic;

import java.util.List;

/**
 * 发放团队收益任务
 */
public interface AsyncDynamicOrderSettlementService {
	/**
	 * bizType=1节点的订单处理
	 * bizType=2提现的业务处理
	 */
	public void sendMessage(List<OrderMsgDO> orderMsgDOList);


	/**
	 * 跨链分发之后的订单处理
	 * @param performanceUpdateVO
	 */
	public void sendMessage(UserPerformanceUpdateVO performanceUpdateVO);
}
