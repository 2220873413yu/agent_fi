package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


/**
 * 注册vo对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterSmsVo implements Serializable {

	/**
	 * 邀请用户编码
	 */
	@NotBlank
	private String inviteUserCode;

	/**
	 * 登录密码(需要md5之后,再传递过来)
	 */
	@NotBlank
	private String loginPwd;

	/**
	 * 账号(6-16位数字和字母组合)
	 */
	@NotBlank
	@Size(min = 6, max = 16, message = "账号不合法")
	private String account;

	/**
	 * 邮箱
	 */
	@NotBlank
	private String email;

	/**
	 * 邮箱验证码
	 */
	@NotBlank
	private String code;

	/**
	 * 验证码的uuid
	 */
	@NotBlank
	private String uuid;
}
