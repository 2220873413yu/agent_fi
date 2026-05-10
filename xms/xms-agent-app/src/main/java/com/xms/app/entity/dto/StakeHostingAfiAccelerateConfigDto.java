package com.xms.app.entity.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * App AFI质押加速配置展示DTO
 */
@Data
public class StakeHostingAfiAccelerateConfigDto {
	@ApiModelProperty(value = "配置ID")
	private Long id;

	@ApiModelProperty(value = "AFI等值USDT / 托管USDT比例，单位%")
	private BigDecimal pledgeRatio;

	@ApiModelProperty(value = "加速倍率，例如1.10")
	private BigDecimal accelerateRate;
}
