package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 托管订单AFI质押参数
 */
@Data
public class PledgeStakeHostingAfiVo {
	/** 托管订单ID */
	@NotNull(message = "托管订单不能为空")
	private Long stakeHostingOrderId;

	/** AFI质押数量 */
	@NotNull(message = "AFI质押数量不能为空")
	private BigDecimal afiAmount;

	/** 钱包签名 */
	@NotBlank(message = "签名不能为空")
	private String signature;

	/** 随机数 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;
}
