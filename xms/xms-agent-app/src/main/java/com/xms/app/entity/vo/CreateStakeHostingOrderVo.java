package com.xms.app.entity.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建托管订单参数
 */
@Data
public class CreateStakeHostingOrderVo {
	/** 托管套餐ID */
	@NotNull(message = "托管套餐不能为空")
	private Long packageId;

	/** 托管USDT金额 */
	@NotNull(message = "托管金额不能为空")
	private BigDecimal amount;

	/** 钱包签名 */
	@NotBlank(message = "签名不能为空")
	private String signature;

	/** 随机数 */
	@NotBlank(message = "随机数不能为空")
	private String randomNum;
}
