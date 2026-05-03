package com.xms.common.config.redis.delayqueue;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author MIER
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedissonDelayOrder<T> {
	private static final long serialVersionUID = 1L;
	/**
	 * 订单ID
	 */
	private String orderId;
	/**
	 * 订单超时多少秒,单位：毫秒
	 */
	private Long startTime;
	/**
	 * 业务类型。 1 延迟双删的干活  2:节点订单延时到期,3:swap订单提现额度增加,4:激活订单延迟删除
	 */
	private Integer bizType;
	/**
	 * msg的body
	 */
	private T data;
	/**
	 * 延迟队列名
	 */
	private String queueName;


}
