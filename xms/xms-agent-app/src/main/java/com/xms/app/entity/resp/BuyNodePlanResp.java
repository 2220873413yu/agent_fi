package com.xms.app.entity.resp;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 认购节点配置响应参数
 *
 * @author xms
 * @date 2026-01-16
 */
@Data
public class BuyNodePlanResp {

	/**
	 * 订单号
	 */
	private String orderNo;

	/**
	 * 金额
	 */
	private BigDecimal amount;
}
