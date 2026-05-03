package com.xms.app.entity.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * 充值接口vo对象
 */
@Data
public class RechargeOrderReq {
	/**
	 * 签名
	 */
	@NotBlank(message = "签名不能为空")
	private String signature;
	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;
	/**
	 *  0:ALEO
	 *  1:usdt  其他根据合约list动态注入
	 */
	@Min(0)
	@NotNull(message = "币种不能为空")
	private Integer bizType;
	/**
	 * 下单金额
	 */
	@NotBlank(message = "下单金额不能为空")
	@Positive
	private String amount;
}
