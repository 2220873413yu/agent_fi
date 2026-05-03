package com.xms.app.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 节点套餐购买回调事件
 */
@Data
public class NodePackageReq {
	/**
	 * hash
	 */
	@NotBlank(message = "hash not null")
	private String txHash;

	/**
	 * orderNo
	 */
	@NotBlank(message = "orderNo not null")
	private String orderNo;

	/**
	 * address
	 */
	@NotBlank
	@NotBlank(message = "address not null")
	private String address;


	/**
	 * 支付价格
	 */
	@NotNull(message = "amount not null")
	private BigDecimal usdtAmount;


	/**
	 * 签名
	 */
	@NotBlank(message = "sign not null")
	private String sign;
}
