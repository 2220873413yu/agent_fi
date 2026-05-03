package com.xms.app.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 空投领取参数
 */
@Data
public class ClaimAirdropReq {

	/** 活动轮次编号 */
	@NotNull
	private Long roundNo;

	/**
	 * 领取的钱包地址
	 */
	@NotBlank
	private String address;

	/**
	 * okb余额
	 */
	@NotNull
	private BigDecimal balance;
}
