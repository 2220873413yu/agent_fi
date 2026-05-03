package com.xms.app.entity.req;

import lombok.Data;

@Data
public class BindGoogleCodeVo {

	/**
	 * 谷歌验证码
	 */
	private String googleCode;

	/**
	 * 密码
	 */
	private String pwd;
}
