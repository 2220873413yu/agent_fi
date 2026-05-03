package com.xms.web.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 基金订单结算的时候(后台拨付订单)
 * @description:
 * @author: xms
 * @date: 2022/7/18 10:09
 */
@Data
public class SourceType0OrderDto {
	/**
	 * 订单id
	 */
	private Long id;
	/**
	 * 待领取金额
	 */
	private BigDecimal awaitingAmount;
}
