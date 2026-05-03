package com.xms.app.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 我的资产信息
 */
@Data
public class MyAssetsBo {
	/** 今日静态总收益 */
	private BigDecimal todayStaticTotalReward;

	/** 今日动态总收益 */
	private BigDecimal todayDynamicTotalReward;

	/** 未出局收益总额 */
	private BigDecimal activeTotalReward;
}
