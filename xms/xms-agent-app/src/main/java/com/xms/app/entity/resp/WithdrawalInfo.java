package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现信息
 */
@Data
public class WithdrawalInfo {
	/**
	 * 待处理提现金额
	 */
	BigDecimal pendingWithdrawAmount;
	/**
	 * 可提现金额
	 */
	private BigDecimal haveWithdrawalBalance;
}
