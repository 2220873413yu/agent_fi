package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台锁定USDT转可用USDT请求。
 */
@Data
public class GrantRewardTransferBo {

	/**
	 * 用户ID。
	 */
	private Long userId;

	/**
	 * 本次转移数量，单位USDT。
	 */
	private BigDecimal transferAmount;
}
