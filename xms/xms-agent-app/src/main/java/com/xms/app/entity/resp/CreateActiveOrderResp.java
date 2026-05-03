package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建激活订单返回参数
 */
@Data
public class CreateActiveOrderResp {

	/**
	 * 支付的激活币
	 */
	private BigDecimal payAmount;


	/**
	 * 订单号
	 */
	private String orderNo;
}
