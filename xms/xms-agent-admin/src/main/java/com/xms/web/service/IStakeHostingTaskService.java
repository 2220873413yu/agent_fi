package com.xms.web.service;

import com.xms.dao.entity.dto.StakeHostingStaticRateTestDto;

import java.util.List;

/**
 * 托管定时任务Service。
 */
public interface IStakeHostingTaskService {
	/**
	 * 每日发放托管静态收益。
	 */
	void distributeDailyStaticReward();

	/**
	 * 每周发放托管全球分红。
	 */
	void distributeWeeklyGlobalDividend();

	/**
	 * 测试计算托管静态日利率。
	 *
	 * <p>该方法只准备/读取G7快照并返回每笔产出中托管订单命中的基础静态日利率，
	 * 不发放奖励、不写钱包、不改订单收益，适合单独配置定时任务核对G7公式。</p>
	 *
	 * @param rewardDay 收益日期，格式yyyyMMdd；为空时默认当天
	 * @return 每笔产出中托管订单的静态日利率测试结果
	 */
	List<StakeHostingStaticRateTestDto> testCalculateStaticRate(Integer rewardDay);
}