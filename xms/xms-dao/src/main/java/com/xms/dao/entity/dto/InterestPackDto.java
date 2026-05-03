package com.xms.dao.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 我的利息包
 * @author xms
 * @date 2023/10/07
 */
@Data
public class InterestPackDto {

	/**
	 * id
	 */
	private Long id;

	/**
	 * 订单号
	 */
	private String orderNo;

	/**
	 * 剩余释放天数
	 */
	private Integer unreleasedDays;

	/**
	 * 总释放天数
	 */
	private Integer totalDays;

	/**
	 * 剩余释放利息
	 */
	private BigDecimal unreleasedAmount;

	private BigDecimal totalAmount;
	private BigDecimal releasedAmount;
	/**
	 * 含义：累计已付燃料比例，相对于 120 天的总比例：
	 * 刚创建：0
	 * 加速到 90 天：0.10
	 * 再加速到 30 天：0.20
	 * 用途：
	 * 算本次加速需要补的比例：delta = newRatio - paid_fuel_ratio；
	 * 避免重复收费（只补差额）。
	 */
	private BigDecimal paidFuelRatio;
}

