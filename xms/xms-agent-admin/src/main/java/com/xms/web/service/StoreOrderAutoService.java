package com.xms.web.service;

import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;

/**
 * 活期基金延迟到账收益任务
 */
public interface StoreOrderAutoService {
	/**
	 * 活期基金延迟到账收益任务
	 * @param order
	 */
	void hanlerOrder(RedissonDelayOrder order);
}
