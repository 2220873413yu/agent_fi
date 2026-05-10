package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @createDate: 2023/7/26 15:18
 */
@Data
public class UserMoneyBo{
	/**
	 * USDT
	 */
	private BigDecimal validNum1;

	/**
	 * AFI
	 */
	private BigDecimal validNum2;
}
