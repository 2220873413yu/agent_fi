package com.xms.web.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 今日基金订单收益
 * @description:
 * @author: xms
 * @date: 2022/7/19 10:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayRewardOrderDto {

	/**
	 * 订单id
	 */
	private Long id;
	/**
	 * 今日收益
	 */
	private BigDecimal reward;
}
