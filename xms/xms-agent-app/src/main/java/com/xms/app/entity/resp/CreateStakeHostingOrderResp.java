package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建托管订单返回
 */
@Data
public class CreateStakeHostingOrderResp {
	/** 托管订单号 */
	private String orderNo;
	/** 托管USDT金额 */
	private BigDecimal stakeUsdtAmount;
}
