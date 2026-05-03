package com.xms.dao.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 团队销毁统计 DTO：同时返回 USDT 和 BOOMAI 数量
 */
@Data
public class TeamDestroyStatDto {

	/**
	 * 团队销毁总价值（USDT）
	 */
	private BigDecimal totalUsdt;

	/**
	 * 团队销毁的 BOOMAI 数量
	 */
	private BigDecimal totalValidNum1;
}


