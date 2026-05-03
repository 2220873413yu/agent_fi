package com.xms.app.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 算力信息
 * @author xms
 * @date 2023/10/23
 */
@Data
public class ComputingPowerBo {

	/**
	 * 全网总算力
	 */
	private BigDecimal globalTotalPower;

	/**
	 * 总算力
	 */
	private BigDecimal totalPower;
	/**
	 * 累计奖励
	 */
	private BigDecimal totalReward;

	/**
	 * 今日奖励
	 */
	private BigDecimal todayReward;
}
