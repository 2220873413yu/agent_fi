package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现页面汇总信息
 */
@Data
public class WithdrawalSummaryResp {

	/**
	 * 累计已提现金额
	 */
	private BigDecimal totalWithdrawal;

	/**
	 * 今日已提现金额
	 */
	private BigDecimal todayWithdrawal;

	/**
	 * 待处理提现金额
	 */
	private BigDecimal pendingAmount;
}

