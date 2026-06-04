package com.xms.app.entity.vo;

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
}
