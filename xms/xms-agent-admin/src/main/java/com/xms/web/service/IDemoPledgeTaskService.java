package com.xms.web.service;

/**
 * 示例质押定时任务Service接口。
 */
public interface IDemoPledgeTaskService {
	/**
	 * 发放示例质押订单每日收益。
	 *
	 * @return 本次成功推进释放进度的订单数量
	 */
	int releaseDailyReward();
}
