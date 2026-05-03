package com.xms.app.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BindEmailVo {

	/**
	 * uuid
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
