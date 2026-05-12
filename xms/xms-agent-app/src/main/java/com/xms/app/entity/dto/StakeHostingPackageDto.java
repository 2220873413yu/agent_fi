package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * App托管套餐展示DTO
 */
@Data
public class StakeHostingPackageDto {

	/**
	 * 套餐id
	 */
	@ApiModelProperty(value = "套餐ID")
	private Long id;

	/**
	 * 套餐名称
	 */
	@ApiModelProperty(value = "套餐名称")
	private String name;

	/**
	 * 托管天数
	 */
	@ApiModelProperty(value = "托管天数")
	private Integer days;

	/**
	 * 最低起购USDT金额
	 */
	@ApiModelProperty(value = "最低起购USDT金额")
	private BigDecimal minAmount;

	/**
	 * 服务费比例，单位%
	 */
	@ApiModelProperty(value = "服务费比例，单位%")
	private BigDecimal serviceFeeRatio;

	/**
	 * 最低日托管比例
	 */
	private BigDecimal minDayRatio = BigDecimal.ZERO;

	/**
	 * 最高日托管比例
	 */
	private BigDecimal maxDayRatio = BigDecimal.ZERO;

	/**
	 * 业绩积分系数，用于计算新增小区业绩积分
	 */
	@ApiModelProperty(value = "业绩积分系数，用于计算新增小区业绩积分")
	private BigDecimal performanceCoefficient;
}
