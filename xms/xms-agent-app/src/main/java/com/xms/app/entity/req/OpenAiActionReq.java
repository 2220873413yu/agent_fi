package com.xms.app.entity.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * open api请求参数
 */
@Data
public class OpenAiActionReq {
	/**
	 * 签名
	 */
	@NotBlank
	private String signature;

	/**
	 * 随机数不能为空
	 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;
}
