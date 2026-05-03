package com.xms.web.service;

import java.util.Map;

/**
 * @author: renengadePISTA
 * @createDate: 2023/9/18
 */
public interface IAsyncTaskService {
	void dealRedisDeadMsg();

	Map<String, Object> getTask(String type, String date);

	int addTask(String type, String date);

	Boolean  transactionExcute(String customerNo, String transactionId);

	void dealSysLogs(Integer days);

	void taskMsgCycle();


	/**
	 * 任务类型100 每天发放矿机奖励
	 */
	void distributePtbInterest100();

	/**
	 * 补偿基金订单赎回本期的时候.t+1时间到了但是还没有执行发放本金任务
	 */
	void compensateUnpaidPrincipalOrders();


	/**
	 * 任务类型101 每天发放质押奖励
	 */
	void distributePtbInterest101();


	/**
	 * 任务类型102 v9节点均分提现手续费分红任务
	 */
	void distributePtbInterest102(Integer parDate);

	/**
	 * 查询没有处理的节点订单
	 */
	void processOverdueMiningOrders();

	/**
	 * 寻找遗漏处理增加团队的业绩矿机订单
	 */
	void task103Handler();

	/**
	 * 补偿任务
	 */
	void task102Handler();

	/**
	 * 重新计算等级的补偿任务
	 */
	void getIdoOrder();
}
