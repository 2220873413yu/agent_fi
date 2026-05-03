package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProcessExchangeVo {
	/**
	 * 源币种数量
	 */
	@Positive
	@NotNull
	private BigDecimal amount;

	/**
	 * 源币种类型 1:USDT, 3:FSN
	 */
	@NotNull
	@ValidDiyStatus(values = {3})
	private Integer fromCoinType;

	/**
	 * 目标币种类型 1:USDT,3:FSN
	 */
	@NotNull
	@ValidDiyStatus(values = {1})
	private Integer toCoinType;

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

//	/**
//	 * 发送短信的账号
//	 */
//	@NotBlank
//	private String account;
//
//	/**
//	 * 手机/邮箱验证码
//	 */
//	@NotBlank
//	private String code;
//
//	/**
//	 * 调用发送短信接口获取的返回值
//	 */
//	@NotBlank
//	private String uuid;
//
//
//	/**
//	 * 交易密码
//	 */
//	@NotBlank
//	private String pwd;
}
