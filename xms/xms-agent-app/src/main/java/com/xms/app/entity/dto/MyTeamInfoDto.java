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
	@ApiModelProperty(value = "当前最大等级编码")
	private Integer currentLevel;

	@ApiModelProperty(value = "目标等级编码")
	private Integer targetLevel;

	@ApiModelProperty(value = "当前个人托管金额")
	private BigDecimal selfHostingAmount;

	@ApiModelProperty(value = "目标个人托管金额")
	private BigDecimal targetSelfHostingAmount;

	@ApiModelProperty(value = "距离目标个人托管还需金额")
	private BigDecimal selfHostingNeedAmount;

	@ApiModelProperty(value = "个人托管进度，单位%")
	private BigDecimal selfHostingProgress;

	@ApiModelProperty(value = "当前团队托管金额")
	private BigDecimal teamHostingAmount;

	@ApiModelProperty(value = "目标团队托管金额")
	private BigDecimal targetTeamHostingAmount;

	@ApiModelProperty(value = "距离目标团队托管还需金额")
	private BigDecimal teamHostingNeedAmount;

	@ApiModelProperty(value = "团队托管进度，单位%")
	private BigDecimal teamHostingProgress;

	@ApiModelProperty(value = "团队人数")
	private Integer teamUserCount;

	@ApiModelProperty(value = "直推人数")
	private Integer directUserCount;

	@ApiModelProperty(value = "团队收益，本期暂不实现，固定返回0")
	private BigDecimal teamRewardAmount;

	@ApiModelProperty(value = "间推收益，当前无间推奖业务，固定返回0")
	private BigDecimal indirectRewardAmount;

	@ApiModelProperty(value = "全球分红累计金额")
	private BigDecimal globalDividendAmount;

	@ApiModelProperty(value = "团队总托管金额")
	private BigDecimal teamTotalHostingAmount;

	@ApiModelProperty(value = "自身托管金额")
	private BigDecimal selfTotalHostingAmount;
}
