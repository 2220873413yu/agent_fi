package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单响应
 *
 * @author xms
 * @date 2021/05/05
 */
@Data
public class CreateOrderResp {

	/**
	 * 订单号
	 */
	private String orderNo;

	/**
	 * 订单金额(支付的u)
	 */
	private BigDecimal usdtValue;

	/**
	 * 到账币种数量
	 */
	private BigDecimal validNum1Value;

	/**
	 * 是否提示重复支付 0:否,1:是
	 */
	private Integer flag;
}
