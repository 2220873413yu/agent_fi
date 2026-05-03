package com.xms.app.entity;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginBo {

	/**
	 * 账号
	 */
	@ApiModelProperty(value = "账号")
	@NotBlank
	private String account;

	/**
	 * 密码
	 */
	@NotBlank
	private String pwd;
}
