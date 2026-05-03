package com.xms.app.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 推荐奖励dto对象
 * @author xms
 * @date 2023/10/05
 */
@Data
public class ReferralRewardDto {

	/**
	 * 上一代
	 */
	private BigDecimal up1 = BigDecimal.ZERO;

	/**
	 * 下一层
	 */
	private BigDecimal l1 = BigDecimal.ZERO;

	/**
	 * 下二层
	 */
	private BigDecimal l2 = BigDecimal.ZERO;

	/**
	 * 直推奖比例 1=1%
	 */
	private BigDecimal directReferral = BigDecimal.ZERO;
}
