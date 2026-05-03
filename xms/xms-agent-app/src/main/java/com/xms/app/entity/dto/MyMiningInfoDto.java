package com.xms.app.entity.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 我的矿机信息
 * @author xms
 * @date 2023/10/07
 */
@Data
public class MyMiningInfoDto {
	/**
	 * 我的矿机数量
	 */
	private Integer miningNum = 0;

	/**
	 * 我的矿机算力
	 */
	private BigDecimal miningComputingPower = BigDecimal.ZERO;

	/**
	 * 已质押矿机数量
	 */
	private Long stakeMiningNum = 0L;

	/**
	 * 已质押矿机算力
	 */
	private BigDecimal stakeMiningComputingPower = BigDecimal.ZERO;

	/** 今日产出DFC */
	private BigDecimal todayOutDfc = BigDecimal.ZERO;
}
