package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * App当前托管静态日利率展示DTO。
 */
@Data
public class CurrentStakeHostingStaticRateDto {
	/**
	 * 收益日期，格式yyyyMMdd。
	 */
	@ApiModelProperty(value = "收益日期，格式yyyyMMdd")
	private Integer rewardDay;

	/**
	 * 今日G值，单位%。
	 */
	@ApiModelProperty(value = "今日G值，单位%")
	private BigDecimal gDay;

	/**
	 * 当前托管基础静态日利率，单位%。
	 */
	@ApiModelProperty(value = "当前托管基础静态日利率，单位%")
	private BigDecimal currentStaticRate;
}
