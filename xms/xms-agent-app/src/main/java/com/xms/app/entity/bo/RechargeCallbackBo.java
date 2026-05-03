package com.xms.app.entity.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值订单支付回调接口
 */
@Data
public class RechargeCallbackBo {

	/**
	 * hash
	 */
	@NotBlank(message = "hash not null")
	private String hash;

	/**
	 * 订单号
	 */
	@NotBlank(message = "订单号")
	private String orderNo;


	/**
	 * 支付金额(价值多少u)
	 */
	@NotNull(message = "amount not null")
	private BigDecimal amount;

	/**
	 * 签名
	 */
	@NotBlank(message = "sign not null")
	private String sign;
}
