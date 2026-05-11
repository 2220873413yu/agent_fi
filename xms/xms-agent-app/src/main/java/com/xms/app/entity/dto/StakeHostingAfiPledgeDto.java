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

	/** 质押记录ID */
	@ApiModelProperty(value = "质押记录ID")
	private Long id;

	/** 质押单号 */
	@ApiModelProperty(value = "质押单号")
	private String pledgeNo;

	/** 托管订单ID */
	@ApiModelProperty(value = "托管订单ID")
	private Long stakeHostingOrderId;

	/** 质押订单号 */
	@ApiModelProperty(value = "托管订单号")
	private String stakeHostingOrderNo;

	/** 托管订单金额快照 */
	@ApiModelProperty(value = "托管订单金额快照")
	private BigDecimal stakeUsdtAmount;

	/** 质押AFI数量 */
	@ApiModelProperty(value = "质押AFI数量")
	private BigDecimal afiAmount;

	/** AFI价格快照 */
	@ApiModelProperty(value = "AFI价格快照")
	private BigDecimal afiPrice;

	/** AFI等值USDT */
	@ApiModelProperty(value = "AFI等值USDT")
	private BigDecimal afiUsdtAmount;

	/** 命中质押比例，单位% */
	@ApiModelProperty(value = "命中质押比例，单位%")
	private BigDecimal pledgeRatio;

	/** 命中加速倍率 */
	@ApiModelProperty(value = "命中加速倍率")
	private BigDecimal accelerateRate;

	/** 质押时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "质押时间")
	private Date pledgeTime;

	/** 生效日期，格式yyyyMMdd */
	@ApiModelProperty(value = "生效日期，格式yyyyMMdd")
	private Integer effectiveDay;

	/** 状态 1:生效中 2:已退还 */
	@ApiModelProperty(value = "状态 1:生效中 2:已退还")
	private Integer status;
}
