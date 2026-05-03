package com.xms.app.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 基金赎回vo对象
 * @author xms
 * @date 2023/10/09
 */
@Data
public class RedeemVo {

	/**
	 * 订单id
	 */
	@NotNull
	private Long id;

	/*
	 * 谷歌验证码
	 */
	@NotBlank(message = "谷歌验证码不能为空")
	private String googleCode;
}
