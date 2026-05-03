package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现vo对象
 * @author xms
 * @date 2023/07/07
 */
@Data
public class WithdrawInfoVo {
	/**
	 * 提现金额
	 */
	@NotNull(message = "amount not null")
	@Positive
	private BigDecimal amount;


	/**
	 * 地址类型 1:USDT,3:FSN
	 */
	@ValidDiyStatus(values = {1,3})
	@NotNull
	private Integer bizType;
	/**
	 * 收款地址
	 */
	@NotBlank
	private String address;

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
