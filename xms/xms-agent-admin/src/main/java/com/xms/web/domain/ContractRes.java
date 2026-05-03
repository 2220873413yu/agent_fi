package com.xms.web.domain;

import lombok.Data;

/**
 * @author: renengadePISTA
 * @createDate: 2024/1/8
 */
@Data
public class ContractRes {
	/**
	 * 订单id
	 */
	private String orderNo;
	/**
	 * 充值类型 1 usdt
	 */
	private String dataType;
	/**
	 * 代币合约地址
	 */
	private String token;
	/**
	 * 充值的用户地址
	 */
	private String from;
	/**
	 * /代币充值到哪个合约
	 */
	private String to;
	/**
	 * 充值数量
	 */
	private String amount;
	/**
	 * 区块编号
	 */
	private String blockNumber;
}
