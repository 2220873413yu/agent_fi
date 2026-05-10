package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * App托管套餐展示DTO
 */
@Data
public class StakeHostingPackageDto {
	@ApiModelProperty(value = "套餐ID")
	private Long id;

	@ApiModelProperty(value = "套餐名称")
	private String name;

	@ApiModelProperty(value = "托管天数")
	private Integer days;

	@ApiModelProperty(value = "最低起购USDT金额")
	private BigDecimal minAmount;

	@ApiModelProperty(value = "服务费比例，单位%")
	private BigDecimal serviceFeeRatio;

	@ApiModelProperty(value = "业绩积分系数，用于计算新增小区业绩积分")
	private BigDecimal performanceCoefficient;
}
