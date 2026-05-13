package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 我的团队数据
 */
@Data
public class MyTeamInfoDto {
	/**
	 * 当前最大等级编码。
	 */
	@ApiModelProperty(value = "当前最大等级编码")
	private Integer currentLevel;

	/**
	 * 目标等级编码。
	 */
	@ApiModelProperty(value = "目标等级编码")
	private Integer targetLevel;

	/**
	 * 当前个人托管金额，单位USDT。
	 */
	@ApiModelProperty(value = "当前个人托管金额")
	private BigDecimal selfHostingAmount;

	/**
	 * 目标个人托管金额，单位USDT。
	 */
	@ApiModelProperty(value = "目标个人托管金额")
	private BigDecimal targetSelfHostingAmount;

	/**
	 * 距离目标个人托管还需金额，单位USDT。
	 */
	@ApiModelProperty(value = "距离目标个人托管还需金额")
	private BigDecimal selfHostingNeedAmount;

	/**
	 * 个人托管进度，单位%。
	 */
	@ApiModelProperty(value = "个人托管进度，单位%")
	private BigDecimal selfHostingProgress;

	/**
	 * 当前团队托管金额，单位USDT。
	 */
	@ApiModelProperty(value = "当前团队托管金额")
	private BigDecimal teamHostingAmount;

	/**
	 * 目标团队托管金额，单位USDT。
	 */
	@ApiModelProperty(value = "目标团队托管金额")
	private BigDecimal targetTeamHostingAmount;

	/**
	 * 距离目标团队托管还需金额，单位USDT。
	 */
	@ApiModelProperty(value = "距离目标团队托管还需金额")
	private BigDecimal teamHostingNeedAmount;

	/**
	 * 团队托管进度，单位%。
	 */
	@ApiModelProperty(value = "团队托管进度，单位%")
	private BigDecimal teamHostingProgress;

	/**
	 * 团队人数。
	 */
	@ApiModelProperty(value = "团队人数")
	private Integer teamUserCount;

	/**
	 * 直推人数。
	 */
	@ApiModelProperty(value = "直推人数")
	private Integer directUserCount;

	/**
	 * 直推收益
	 */
	@ApiModelProperty(value = "直推收益")
	private BigDecimal directRewardAmount;

	/**
	 * 团队收益，等于托管极差奖累计加托管平级奖累计，单位USDT。
	 */
	@ApiModelProperty(value = "团队收益，托管极差奖累计+托管平级奖累计")
	private BigDecimal teamRewardAmount;

	/**
	 * 托管全球分红累计金额，单位USDT。
	 */
	@ApiModelProperty(value = "全球分红累计金额")
	private BigDecimal globalDividendAmount;

	/**
	 * 团队总托管金额，单位USDT。
	 */
	@ApiModelProperty(value = "团队总托管金额")
	private BigDecimal teamTotalHostingAmount;

	/**
	 * 自身托管金额，单位USDT。
	 */
	@ApiModelProperty(value = "自身托管金额")
	private BigDecimal selfTotalHostingAmount;
}
