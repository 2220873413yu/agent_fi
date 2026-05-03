package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IncomeInfoResp {

	/**
	 * 本月收益
	 */
	private BigDecimal montyReward;

	/**
	 * 昨日收益
	 */
	private BigDecimal yesterdayReward;
}
