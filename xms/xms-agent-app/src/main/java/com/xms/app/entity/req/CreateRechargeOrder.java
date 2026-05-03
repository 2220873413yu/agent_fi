package com.xms.app.entity.req;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建充值订单请求参数
 */
@Data
public class CreateRechargeOrder {
	/**
	 * 充值金额
	 */
	@NotNull
	private BigDecimal rechargeAmount;

	/**
	 * 签名
	 */
	@ApiModelProperty(value = "签名")
	@NotBlank
	private String signature;


	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;
}
