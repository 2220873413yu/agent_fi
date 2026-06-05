package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * App停止1天自动复投托管订单参数。
 */
@Data
public class StopStakeHostingOrderVo {
	/** 托管订单ID */
	@NotNull(message = "托管订单不能为空")
	private Long orderId;

	/** 钱包签名 */
	@NotBlank(message = "签名不能为空")
	private String signature;

	/** 随机数 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;
}
