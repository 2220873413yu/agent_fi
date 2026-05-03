package com.xms.app.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 团队数据
 */
@Data
public class TeamViewBO {

	/**
	 * 直推业绩
	 */
	private BigDecimal subPerformance;

	/**
	 * 小区业绩
	 */
	private BigDecimal communityPerformance;

	/**
	 * 团队业绩
	 */
	private BigDecimal umbrellaPerformance;
	/**
	 * 团队人数
	 */
	private Integer umbrellaNum;
	/**
	 * 直推人数
	 */
	private Integer subNum;

	/**
	 * 直推奖励
	 */
	private BigDecimal subReward;

	/**
	 * 间接奖励
	 */
	private BigDecimal indirectReward;
}
