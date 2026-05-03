package com.xms.app.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HomeMiningOrderVo {
	/**
	 * 当前日利率
	 */
	private BigDecimal todayRatio = BigDecimal.ZERO;

	/**
	 * 当前质押总本金
	 */
	private BigDecimal totalFsnValue = BigDecimal.ZERO;

	/**
	 * 今日总利息
	 */
	private BigDecimal todayTotalInterest = BigDecimal.ZERO;

	/**
	 * 当前总余额宝
	 */
	private BigDecimal currentValidNum3 = BigDecimal.ZERO;

	/**
	 * 当前总剩余收益上限
	 */
	private BigDecimal totalHaveFsnMultipliedValue = BigDecimal.ZERO;
}
