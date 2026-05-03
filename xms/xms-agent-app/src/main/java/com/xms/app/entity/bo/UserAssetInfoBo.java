package com.xms.app.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户资产信息 DTO
 *
 * 说明：当前仅作为返回结构占位，后续可按需补充字段。
 */
@Data
public class UserAssetInfoBo {

	/**
	 * 全网销毁价值usdt量
	 */
	private BigDecimal totalUsdtValue;

	/**
	 * 团队日新增业绩(usdt)
	 */
	private BigDecimal todayTeamUsdtValue;

	/**
	 * 团队日新增业绩(BOOMAI)
	 */
	private BigDecimal todayTeamValidNum1Value;

	/**
	 * 团队月新增业绩（当月、usdt）
	 */
	private BigDecimal monthTeamUsdtValue;

	/**
	 * 团队月新增业绩（当月、BOOMAI）
	 */
	private BigDecimal monthTeamValidNum1Value;

	/**
	 * 账户可用BOOMAI
	 */
	private BigDecimal validNum1Value;
}


