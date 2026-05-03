package com.xms.common.core.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsDataDay {
	/**
	 * 时间 年月日
	 */
	private String time;
	/**
	 * fil数量
	 */
	private BigDecimal filNum;
	/**
	 * usdt数量
	 */
	private BigDecimal usdtNum;

}
