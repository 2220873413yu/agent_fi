package com.xms.app.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * swap订单回调请求参数
 */
@Data
public class SwapOrderCallbackReq {

	/**
	 * hash
	 */
	@NotBlank(message = "hash not null")
	private String hash;

	/**
	 * address
	 */
	@NotBlank(message = "address")
	private String address;


	/**
	 * 兑换数量
	 */
	@NotNull(message = "amount not null")
	private BigDecimal swapAmount;

	/**
	 * 签名
	 */
	@NotBlank(message = "sign not null")
	private String sign;
}
