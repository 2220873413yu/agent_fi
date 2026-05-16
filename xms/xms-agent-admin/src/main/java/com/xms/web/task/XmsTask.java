package com.xms.web.task;

import com.xms.common.utils.StringUtils;
import com.xms.web.service.IAsyncTaskService;
import com.xms.web.service.IAsyncUserUpgradeService;
import com.xms.web.service.ScheduleTaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 定时调度任务处理
 *
 * @author: renegadePISTA
 * @createDate: 2023/9/9
 */
@Slf4j
@Component("xmsTask")
@AllArgsConstructor
public class XmsTask {
	private final IAsyncTaskService asyncTaskServiceImpl;
	private final IAsyncUserUpgradeService asyncUserUpgradeServiceImpl;
	private final ScheduleTaskService scheduleTaskServiceImpl;
	private final com.xms.web.service.IStakeHostingTaskService stakeHostingTaskServiceImpl;
	private final com.xms.dao.service.IPolymarketOrderService polymarketOrderService;
	@Deprecated
	public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i) {
		System.out.println(StringUtils.format("执行多参方法： 字符串类型{}，布尔类型{}，长整型{}，浮点型{}，整形{}", s, b, l, d, i));
	}

	@Deprecated
	public void ryParams(String params) {
		System.out.println("执行有参方法：" + params);
	}

	@Deprecated
	public void ryNoParams() {
		System.out.println("执行无参方法");
	}


	/**
	 * 清楚XX天前的日志
	 */
	public void dealSysLogs(Integer days) throws Exception {
		log.info("清除 {} 天前的日志", days);
		asyncTaskServiceImpl.dealSysLogs(days);
	}

	/**
	 * 处理消费者阻塞进入死信队列的消息
	 */
	public void dealRedisDeadMsg() throws Exception {
		log.info("处理消费者阻塞进入死信队列的消息");
		asyncTaskServiceImpl.dealRedisDeadMsg();
	}

	/**
	 * 处理事务消息阻塞的
	 */
	public void taskMsgCycle() throws Exception {
		log.info("处理事务消息阻塞的的消息");
		asyncTaskServiceImpl.taskMsgCycle();
	}


//	/**
//	 * 任务类型100 每天发放矿机奖励
//	 *
//	 */
//	public void distributePtbInterest100() {
//		log.info("任务类型100 每天发放矿机奖励");
//		asyncTaskServiceImpl.distributePtbInterest100();
//	}

	/**
	 * 任务类型101 每天发放质押奖励
	 *
	 */
	public void distributePtbInterest101() {
		log.info("任务类型101 每天发放托管静态收益");
		stakeHostingTaskServiceImpl.distributeDailyStaticReward();
	}

	/**
	 * 任务类型102 每周天晚上24点结算
	 *
	 */
	public void distributePtbInterest102() {
		log.info("任务类型102 每周天晚上24点结算");
		stakeHostingTaskServiceImpl.distributeWeeklyGlobalDividend();
	}

	/**
	 * 测试托管静态日利率。
	 *
	 * <p>该任务只调用测试方法准备/读取G7快照并打印每笔产出中订单命中的基础静态日利率，
	 * 不发放奖励、不写钱包、不改订单收益字段。</p>
	 */
	public void testStakeHostingStaticRate() {
		log.info("测试托管静态日利率");
		stakeHostingTaskServiceImpl.testCalculateStaticRate(null);
	}

	/**
	 * 结算Polymarket平台内部订单。
	 *
	 * <p>该方法由RuoYi定时任务调用。它扫描已到结束时间的待结算订单，查询Polymarket最终结果；
	 * 猜中订单兑付USDT到用户validNum1，结果不明确的订单转入待人工复核。</p>
	 */
	public void settlePolymarketOrders() {
		log.info("settle Polymarket local orders");
		// 每次任务最多处理100笔，避免单次Quartz任务执行时间过长。
		int count = polymarketOrderService.settlePendingOrders(100);
		log.info("settled Polymarket local orders, updated={}", count);
	}


	/**
	 * 查询没有处理的节点订单
	 */
	public void processOverdueMiningOrders() {
		log.info("查询没有处理的节点订单");
		asyncTaskServiceImpl.processOverdueMiningOrders();
	}

	/**
	 * 补偿业务(重新计算等级先关)
	 */
	public void getIdoOrder() {
		log.info("重新计算等级相关");
		asyncTaskServiceImpl.getIdoOrder();
	}

	/**
	 * 补偿业务(重新计算等级先关)
	 */
	public void getIdoOrder1() {
		log.info("重新计算等级相关");
		asyncTaskServiceImpl.getIdoOrder1();
	}

	/**
	 * 任务类型101 每日增加算力(订单天数的n次方)
	 */
//	public void distributePtbInterest101() {
//		log.info("任任务类型101 每日增加算力(订单天数的n次方)");
//		asyncTaskServiceImpl.distributePtbInterest101();
//	}

//	/**
//	 * 任务类型102 v9节点均分提现手续费分红任务
//	 *
//	 */
//	public void distributePtbInterest102(Integer parDate) {
//		log.info("任务类型102 v9节点均分提现手续费分红任务");
//		asyncTaskServiceImpl.distributePtbInterest102(parDate);
//	}

//	/**
//	 * 任务类型103 每日统计平台币价格
//	 *
//	 */
//	public void dailyPlatformCoinPriceRecord103 () {
//		log.info("任务类型103 每日统计平台币价格");
//		asyncTaskServiceImpl.dailyPlatformCoinPriceRecord103();
//	}



//	/**
//	 * 寻找遗漏处理增加团队的业绩矿机订单.
//	 */
//	public void task103Handler() {
//		// 寻找遗漏处理增加团队的业绩矿机订单
//		asyncTaskServiceImpl.task103Handler();
//	}

	/**
	 * 补偿基金订单赎回本期的时候.t+1时间到了但是还没有执行发放本金任务  0/20 * * * * ?
	 */
//	public void compensateUnpaidPrincipalOrders() {
//		// 补偿基金订单赎回本期的时候.t+1时间到了但是还没有执行发放本金任务
//		log.info("补偿基金订单赎回本期的时候.t+1时间到了但是还没有执行发放本金任务");
//		asyncTaskServiceImpl.compensateUnpaidPrincipalOrders();
//	}




//	/**
//	 * 补偿任务
//	 *
//	 */
//	public void task102Handler() {
//		asyncTaskServiceImpl.task102Handler();
//	}

//
//	/**
//	 * 任务类型103 复利手续费分红
//	 * 20%平台沉淀
//	 * 20%合伙人平均分
//	 * 30%级别加权分红（同级别就平均分）
//	 * 30%给积分≥30分的用户加权分红（分数可配置）
//	 *
//	 */
//	public void task103Handler() {
//		// 任务类型103 复利手续费分红
//		log.info("任务类型103 复利手续费分红");
//		asyncTaskServiceImpl.task103Handler();
//	}
//
//	/**
//	 * 任务类型104 卖币手续费分红
//	 * 40%合伙人平均分
//	 * 60%留存（每一笔要有留存记录，并且后台需要进行汇总统计--方便2.0链上处理）
//	 *
//	 */
//	public void task104Handler() {
//		// 任务类型104 卖币手续费分红
//		log.info("任务类型104 卖币手续费分红");
//		asyncTaskServiceImpl.task104Handler();
//	}
//
//	public void task105Handler() {
//		asyncTaskServiceImpl.task105Handler();
//	}
}
