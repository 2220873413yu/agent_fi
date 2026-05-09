package com.xms.web.service;

/**
 * 托管定时任务Service
 */
public interface IStakeHostingTaskService {
	/**
	 * 每日发放托管静态收益。
	 * 第一批使用占位收益率，后续替换为G7收益率。
	 */
	void distributeDailyStaticReward();

	/**
	 * 每周发放托管全球分红。
	 */
	void distributeWeeklyGlobalDividend();
}
