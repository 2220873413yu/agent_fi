package com.xms.app.entity.vo;

import lombok.Data;

/**
 * 更新提现地址
 */
@Data
public class UpdateWithdrawalAddress {
	/**
	 * bep地址
	 */
	private String withdrawalBepAddress;
	/**
	 * trc地址
	 */
	private String withdrawalTrcAddress;
}
