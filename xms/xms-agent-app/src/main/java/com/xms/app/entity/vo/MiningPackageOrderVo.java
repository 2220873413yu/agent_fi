package com.xms.app.entity.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MiningPackageOrderVo {
	/***
	 * 债券套餐id
	 */
	@NotNull
	private Long id;

	/***
	 * 购买金额
	 * 必须是10的倍数
	 */
	@NotNull
	@Positive
	@Min(10)
	private BigDecimal amount;

	/**
	 * 发送短信的账号
	 */
	@NotBlank
	private String account;

	/**
	 * 手机/邮箱验证码
	 */
	@NotBlank
	private String code;

	/**
	 * 调用发送短信接口获取的返回值
	 */
	@NotBlank
	private String uuid;


	/**
	 * 交易密码
	 */
	@NotBlank
	private String pwd;
}
