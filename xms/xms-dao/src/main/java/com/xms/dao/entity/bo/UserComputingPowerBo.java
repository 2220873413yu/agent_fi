package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserComputingPowerBo {
	/**
	 * 用户id
	 */
	private Long userId;
	/**
	 * 用户算力
	 */
	private BigDecimal computingPower;
}

