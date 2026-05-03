package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 空投领取结果
 */
@Data
public class ClaimAirdropResp {

	/**
	 * 订单号
	 */
	private String orderNo;

	/**
	 * 每次领取需支付的OKB数量（按USDT价值计算）
	 */
	private BigDecimal okbAmountByUsdtValue;
}
