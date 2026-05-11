package com.xms.app.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * App AFI质押加速记录展示DTO
 */
@Data
public class StakeHostingAfiPledgeDto {
	@ApiModelProperty(value = "质押记录ID")
	private Long id;

	@ApiModelProperty(value = "质押单号")
	private String pledgeNo;

	@ApiModelProperty(value = "托管订单ID")
	private Long stakeHostingOrderId;

	@ApiModelProperty(value = "托管订单号")
	private String stakeHostingOrderNo;

	@ApiModelProperty(value = "托管订单金额快照")
	private BigDecimal stakeUsdtAmount;

	@ApiModelProperty(value = "质押AFI数量")
	private BigDecimal afiAmount;

	@ApiModelProperty(value = "AFI价格快照")
	private BigDecimal afiPrice;

	@ApiModelProperty(value = "AFI等值USDT")
	private BigDecimal afiUsdtAmount;

	@ApiModelProperty(value = "命中质押比例，单位%")
	private BigDecimal pledgeRatio;

	@ApiModelProperty(value = "命中加速倍率")
	private BigDecimal accelerateRate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "质押时间")
	private Date pledgeTime;

	@ApiModelProperty(value = "生效日期，格式yyyyMMdd")
	private Integer effectiveDay;

	@ApiModelProperty(value = "状态 1:生效中 2:已退还")
	private Integer status;
}
