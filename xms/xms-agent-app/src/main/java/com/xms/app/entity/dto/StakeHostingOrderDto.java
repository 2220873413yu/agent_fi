package com.xms.app.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * App托管订单展示DTO
 */
@Data
public class StakeHostingOrderDto {
	@ApiModelProperty(value = "订单ID")
	private Long id;

	@ApiModelProperty(value = "托管订单号")
	private String orderNo;

	@ApiModelProperty(value = "套餐ID")
	private Long packageId;

	@ApiModelProperty(value = "套餐名称快照")
	private String packageName;

	@ApiModelProperty(value = "套餐天数快照")
	private Integer packageDays;

	@ApiModelProperty(value = "托管USDT金额")
	private BigDecimal stakeUsdtAmount;

	@ApiModelProperty(value = "业绩积分系数快照")
	private BigDecimal performanceCoefficient;

	@ApiModelProperty(value = "业绩积分快照")
	private BigDecimal performancePoints;

	@ApiModelProperty(value = "订单来源 0:用户购买 1:后台拨付")
	private Integer sourceType;

	@ApiModelProperty(value = "支付状态 0:待支付 1:支付成功 2:后台拨付无需支付")
	private Integer payStatus;

	@ApiModelProperty(value = "业务状态 0:未开始 1:产出中 2:已完成 3:已暂停")
	private Integer status;

	@ApiModelProperty(value = "支付hash")
	private String payHash;

	@ApiModelProperty(value = "链上支付金额")
	private BigDecimal payAmount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "支付时间")
	private Date payTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "生效时间")
	private Date effectiveTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "完成时间")
	private Date finishTime;

	@ApiModelProperty(value = "已运行天数，即已发放静态收益次数")
	private Integer runDays;

	@ApiModelProperty(value = "今日静态收益")
	private BigDecimal todayReward;

	@ApiModelProperty(value = "累计已发静态收益")
	private BigDecimal totalStaticReward;

	@ApiModelProperty(value = "是否回本 0:否 1:是")
	private Integer isReturnPrincipal;

	@ApiModelProperty(value = "是否已绑定AFI加速 0:否 1:是")
	private Integer afiAccelerated;

	@ApiModelProperty(value = "最近一次发放日期，格式yyyyMMdd")
	private Integer lastRewardDay;
}
