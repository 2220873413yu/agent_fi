package com.xms.app.entity.vo;

import com.xms.common.annotation.ValidDiyStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountVo {
	/**
	 * 类型 1:手机号,2:邮箱
	 */
	@ValidDiyStatus(values = {1,2})
	private Integer type;

	/**
	 * 手机/邮箱验证码
	 */
	@NotBlank
	private String code;

	/**
	 * 账号(手机号/邮箱)
	 */
	@NotBlank
	private String account;

	/**
	 * 调用发送短信接口获取的返回值
	 */
	@NotBlank
	private String uuid;
}
