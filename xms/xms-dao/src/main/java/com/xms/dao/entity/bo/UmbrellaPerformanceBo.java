package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UmbrellaPerformanceBo {
	/**
	 * 业绩
	 */
	private BigDecimal reward;
	// 父级链 格式为1,2,3
	private String parentChain;

	/**
	 * 用户id
	 */
	private Long userId;
}
