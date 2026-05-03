package com.xms.dao.entity.req;

import lombok.Data;

/**
 * 后台管理拨付矿机
 * @description:
 * @author: xms
 * @date: 2026/2/21 10:05
 */
@Data
public class AdminAllocateMiningMachineRequest {
	/**
	 * 钱包地址
	 */
	private String account;

	/**
	 * 支付方式 1:usdt,2:dfc
	 */
	private Integer payType;
}
