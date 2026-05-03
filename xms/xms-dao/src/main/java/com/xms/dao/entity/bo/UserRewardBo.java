package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserRewardBo {
	private Long userId;
	/**
	 * 静态奖励
	 */
	private BigDecimal staticReward = BigDecimal.ZERO;

	/**
	 * 团队奖励
	 */
	private BigDecimal teamReward = BigDecimal.ZERO;

	/**
	 * 分享奖(互动奖)
	 */
	private BigDecimal interactReward = BigDecimal.ZERO;
}
