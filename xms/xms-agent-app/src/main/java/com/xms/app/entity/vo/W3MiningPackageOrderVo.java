package com.xms.app.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class W3MiningPackageOrderVo {
	/***
	 * 债券套餐id
	 */
	@NotNull
	private Long id;

	/***
	 * 支付金额
	 */
	@NotNull
	@Positive
	private BigDecimal amount;

	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;

	/**
	 * 签名
	 */
	@ApiModelProperty(value = "签名")
	@NotBlank
	private String signature;
}
