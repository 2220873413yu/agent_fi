package com.xms.app.entity.vo;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 忘记密码
 */
@Data
public class ForgotPasswordVo {
	/**
	 * 新密码(MD5加密)
	 */
	@ApiModelProperty(value = "新密码(MD5加密)",required=true)
	@NotBlank
	private String newPwd;

	/**
	 * 谷歌验证码
	 */
	@NotBlank
	private String googleCode;

	/**
	 * 账号
	 */
	@NotBlank
	private String account;

	/**
	 * 验证码的uuid
	 */
	@NotBlank
	private String uuid;

	/**
	 * 邮箱
	 */
	@NotBlank
	private String email;

	/**
	 * 验证码
	 */
	@NotBlank
	private String code;
}
