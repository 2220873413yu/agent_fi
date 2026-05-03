package com.xms.app.entity.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提现接口回调
 */
@Data
public class WithdrawalCallbackBo {

	/**
	 * 交易hash
	 */
	@NotBlank(message = "hash not null")
	private String hash;

	/**
	 * 提现订单号
	 */
	@NotBlank(message = "requestId not null")
	private String requestId;

	/**
	 * 签名
	 */
	@NotBlank(message = "sign not null")
	private String sign;

	/**
	 * 提现状态 true:提现成功,false:提现失败
	 */
	@NotNull
	private Boolean success;

}
