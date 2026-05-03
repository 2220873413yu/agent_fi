package com.xms.dao.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: renengadePISTA
 * @createDate: 2024/1/10
 */
@Data
public class PriceGroupDto {
	/**
	 * u兑换 aleo价格
	 */
	private BigDecimal uToDog;
	/**
	 *
	 */
	private BigDecimal uToIdm;

	/**
	 * aleo价格
	 */
	private BigDecimal aleoPrice;
	/**
	 *
	 */
	private BigDecimal dofiToIdm;
	private String msg;
}
