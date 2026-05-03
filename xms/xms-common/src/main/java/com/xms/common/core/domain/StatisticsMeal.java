package com.xms.common.core.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatisticsMeal {
	/**
	 * 套餐天数
	 */
	private Integer mealDay;

	/**
	 * 今日进单量 fil
	 */
	private BigDecimal todayInputNum = new BigDecimal(0);
	/**
	 * 今日进单量 usdt
	 */
	private BigDecimal todayInputNumUsdt = new BigDecimal(0);

	/**
	 * 总进单量 fil
	 */
	private BigDecimal totalInputNum = new BigDecimal(0);
	/**
	 * 总进单量 usdt
	 */
	private BigDecimal totalInputNumUsdt = new BigDecimal(0);


}
