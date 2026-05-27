package com.xms.web.task;

import com.xms.web.service.IDemoPledgeTaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 示例质押定时任务入口。
 *
 * <p>Quartz 配置时可直接调用该 Bean 的方法，例如：demoPledgeTask.releaseDemoPledgeRewardDaily。</p>
 */
@Slf4j
@Component("demoPledgeTask")
@AllArgsConstructor
public class DemoPledgeTask {
	private final IDemoPledgeTaskService demoPledgeTaskService;

	/**
	 * 每日发放示例质押收益。
	 *
	 * <p>该入口只负责记录任务日志并调用业务服务，具体扫描、幂等、钱包批量入账和奖励记录落库都在服务层完成。</p>
	 */
	public void releaseDemoPledgeRewardDaily() {
		log.info("开始发放示例质押每日收益");
		int count = demoPledgeTaskService.releaseDailyReward();
		log.info("示例质押每日收益发放完成，释放订单数：{}", count);
	}
}
