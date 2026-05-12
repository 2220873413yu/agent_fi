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
	/**
	 * 订单ID
	 */
	@ApiModelProperty(value = "订单ID")
	private Long id;

	/**
	 * 订单号
	 */
	@ApiModelProperty(value = "托管订单号")
	private String orderNo;

	/**
	 * 套餐ID
	 */
	@ApiModelProperty(value = "套餐ID")
	private Long packageId;

	/**
	 * 套餐名称
	 */
	@ApiModelProperty(value = "套餐名称快照")
	private String packageName;

	/**
	 * 套餐天数
	 */
	@ApiModelProperty(value = "套餐天数快照")
	private Integer packageDays;

	/**
	 * 托管USDT金额
	 */
	@ApiModelProperty(value = "托管USDT金额")
	private BigDecimal stakeUsdtAmount;

	/**
	 * 服务费比例，单位%
	 */
	@ApiModelProperty(value = "服务费比例快照，单位%")
	private BigDecimal serviceFeeRatio;

	/**
	 * 业绩积分系数
	 */
	@ApiModelProperty(value = "业绩积分系数快照")
	private BigDecimal performanceCoefficient;

	/**
	 * 业绩积分
	 */
	@ApiModelProperty(value = "业绩积分快照")
	private BigDecimal performancePoints;

	/**
	 * 订单来源 0:用户购买 1:后台拨付
	 */
	@ApiModelProperty(value = "订单来源 0:用户购买 1:后台拨付")
	private Integer sourceType;

	/**
	 * 支付状态 0:待支付 1:支付成功
	 */
	@ApiModelProperty(value = "支付状态 0:待支付 1:支付成功")
	private Integer payStatus;

	/**
	 * 业务状态 0:未开始 1:产出中 2:已完成 3:已暂停
	 */
	@ApiModelProperty(value = "业务状态 0:未开始 1:产出中 2:已完成 3:已暂停")
	private Integer status;

	/**
	 * 支付hash
	 */
	@ApiModelProperty(value = "支付hash")
	private String payHash;

	/**
	 * 链上支付金额
	 */
	@ApiModelProperty(value = "链上支付金额")
	private BigDecimal payAmount;

	/**
	 * 支付时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "支付时间")
	private Date payTime;

	/**
	 * 生效时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "生效时间")
	private Date effectiveTime;

	/**
	 * 完成时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "完成时间")
	private Date finishTime;

	/**
	 * 已运行天数
	 */
	@ApiModelProperty(value = "已运行天数，即已发放静态收益次数")
	private Integer runDays;

	/**
	 * 今日静态收益
	 */
	@ApiModelProperty(value = "今日静态收益")
	private BigDecimal todayReward;

	/**
	 * 累计已发静态收益
	 */
	@ApiModelProperty(value = "累计已发静态收益")
	private BigDecimal totalStaticReward;

	/**
	 * 是否回本 0:否 1:是
	 */
	@ApiModelProperty(value = "是否回本 0:否 1:是")
	private Integer isReturnPrincipal;

	/**
	 * 是否已绑定AFI加速 0:否 1:是
	 */
	@ApiModelProperty(value = "是否已绑定AFI加速 0:否 1:是")
	private Integer afiAccelerated;

	/**
	 * 最近一次发放日期，格式yyyyMMdd
	 */
	@ApiModelProperty(value = "最近一次发放日期，格式yyyyMMdd")
	private Integer lastRewardDay;
}
