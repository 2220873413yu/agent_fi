package com.xms.web.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 每日基金释放利息vo对象
 */
@Data
public class OrderInterestDto {
	/**
	 * 来源订单号
	 */
	private String orderNo;

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 变动金额
	 */
	private BigDecimal amount;

	/**
	 * 0:活期矿机,1:封闭式基金,2:体验式基金
	 */
	private Integer type;
}
