package com.xms.app.entity.bo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 节点订单回调参数
 */
@Data
public class StakeOrderBo {
	/**
	 * 订单号
	 */
	@NotBlank(message = "orderNo not null")
	private String orderNo;

	/**
	 * hash
	 */
	@NotBlank(message = "hash not null")
	private String hash;

	/**
	 * 钱包地址
	 */
	@NotBlank
	private String address;

	/**
	 * 签名
	 */
	@NotBlank(message = "sign not null")
	private String sign;

	/**
	 * 支付金额
	 */
	@NotNull(message = "amount not null")
	private BigDecimal amount;
}
