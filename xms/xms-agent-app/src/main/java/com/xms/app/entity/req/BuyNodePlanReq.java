package com.xms.app.entity.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 认购节点配置请求参数
 *
 * @author xms
 * @date 2026-01-16
 */
@Data
public class BuyNodePlanReq {

	/**
	 * 钱包地址
	 */
	@NotBlank
	private String address;

	/**
	 * 节点配置id
	 */
	@NotNull
	private Long nodePlanId;
	/**
	 * 钱包余额
	 */
	@NotNull
	private BigDecimal amount;
}
