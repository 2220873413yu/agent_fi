package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 托管订单对象 t_stake_hosting_order
 *
 * @author xms
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_stake_hosting_order")
public class StakeHostingOrder extends BaseEntity {
	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 托管订单号 */
	@Excel(name = "托管订单号", sort = 2, width = 30)
	@ApiModelProperty(value = "托管订单号")
	private String orderNo;

	/** 用户ID */
	@Excel(name = "用户ID", sort = 3)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	/** 用户钱包地址 */
	@Excel(name = "钱包地址", sort = 4, width = 40)
	@ApiModelProperty(value = "用户钱包地址")
	private String account;

	/** 套餐ID */
	@Excel(name = "套餐ID", sort = 5)
	@ApiModelProperty(value = "套餐ID")
	private Long packageId;

	/** 套餐名称快照 */
	@Excel(name = "套餐名称", sort = 6)
	@ApiModelProperty(value = "套餐名称快照")
	private String packageName;

	/** 套餐天数快照 */
	@Excel(name = "套餐天数", sort = 7)
	@ApiModelProperty(value = "套餐天数快照")
	private Integer packageDays;

	/** 托管USDT金额 */
	@Excel(name = "托管USDT金额", sort = 8)
	@ApiModelProperty(value = "托管USDT金额")
	private BigDecimal stakeUsdtAmount;

	/** Service fee ratio snapshot, unit %. Settlement jobs must read this order snapshot instead of current package config. */
	@Excel(name = "服务费比例", sort = 9)
	@ApiModelProperty(value = "服务费比例快照，单位%")
	private BigDecimal serviceFeeRatio;

	/** 业绩积分系数快照 */
	@Excel(name = "业绩积分系数", sort = 9)
	@ApiModelProperty(value = "业绩积分系数快照")
	private BigDecimal performanceCoefficient;

	/** 业绩积分快照 */
	@Excel(name = "业绩积分", sort = 10)
	@ApiModelProperty(value = "业绩积分快照")
	private BigDecimal performancePoints;

	/** 订单来源 0:用户购买 1:后台拨付 */
	@Excel(name = "订单来源", sort = 9, dictType = "t_stake_hosting_order_source_type")
	@ApiModelProperty(value = "订单来源 0:用户购买 1:后台拨付")
	private Integer sourceType;

	/** 支付状态 0:待支付 1:支付成功 */
	@Excel(name = "支付状态", sort = 10, dictType = "t_stake_hosting_order_pay_status")
	@ApiModelProperty(value = "支付状态 0:待支付 1:支付成功")
	private Integer payStatus;

	/** 业务状态 0:未开始 1:产出中 2:已完成 3:已暂停 */
	@Excel(name = "业务状态", sort = 11, dictType = "t_stake_hosting_order_status")
	@ApiModelProperty(value = "业务状态 0:未开始 1:产出中 2:已完成 3:已暂停")
	private Integer status;

	/** 支付hash */
	@Excel(name = "支付hash", sort = 12, width = 40)
	@ApiModelProperty(value = "支付hash")
	private String payHash;

	/** 链上支付金额 */
	@Excel(name = "链上支付金额", sort = 13)
	@ApiModelProperty(value = "链上支付金额")
	private BigDecimal payAmount;

	/** 支付时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "支付时间", sort = 14, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date payTime;

	/** 生效时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "生效时间", sort = 15, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date effectiveTime;

	/** 完成时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "完成时间", sort = 16, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date finishTime;

	/** 已运行天数，即已发放静态收益次数 */
	@Excel(name = "已运行天数", sort = 17)
	@ApiModelProperty(value = "已运行天数，即已发放静态收益次数")
	private Integer runDays;

	/** 今日静态收益 */
	@Excel(name = "今日静态收益", sort = 18)
	@ApiModelProperty(value = "今日静态收益")
	private BigDecimal todayReward;

	/** 累计已发静态收益 */
	@Excel(name = "累计静态收益", sort = 19)
	@ApiModelProperty(value = "累计已发静态收益")
	private BigDecimal totalStaticReward;

	/** 是否回本 0:否 1:是 */
	@Excel(name = "是否回本", sort = 20, dictType = "t_stake_hosting_order_return_principal")
	@ApiModelProperty(value = "是否回本 0:否 1:是")
	private Integer isReturnPrincipal;

	/** 是否已绑定AFI加速 0:否 1:是 */
	@Excel(name = "AFI加速", sort = 21, dictType = "t_stake_hosting_order_afi_accelerated")
	@ApiModelProperty(value = "是否已绑定AFI加速 0:否 1:是")
	private Integer afiAccelerated;

	/** 最近一次发放日期，格式yyyyMMdd */
	@Excel(name = "最近发放日期", sort = 22)
	@ApiModelProperty(value = "最近一次发放日期，格式yyyyMMdd")
	private Integer lastRewardDay;

	/** 创建日期，格式yyyyMMdd */
	@Excel(name = "创建日期", sort = 23)
	@ApiModelProperty(value = "创建日期，格式yyyyMMdd")
	private Long performanceStartTime;

	/** 周业绩结束时间，格式yyyyMMddHHmmss */
	@ApiModelProperty(value = "周业绩结束时间，格式yyyyMMddHHmmss")
	private Long performanceEndTime;

	/** 周新增小区业绩处理状态 0:未处理 1:队列中 2:处理中 3:已处理 4:处理失败 */
	@ApiModelProperty(value = "周新增小区业绩处理状态 0:未处理 1:队列中 2:处理中 3:已处理 4:处理失败")
	@TableField(exist = false)
	private Integer weeklyPerformanceStatus;

	/** 周新增小区业绩跳过/失败原因 */
	@ApiModelProperty(value = "周新增小区业绩跳过/失败原因")
	@TableField(exist = false)
	private String weeklyPerformanceSkipReason;

	/** 周新增小区业绩处理时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "周新增小区业绩处理时间")
	@TableField(exist = false)
	private Date weeklyPerformanceTime;

	/** 周到期小区业绩重算状态 0:未处理 1:队列中 2:处理中 3:已处理 4:处理失败 */
	@ApiModelProperty(value = "周到期小区业绩重算状态 0:未处理 1:队列中 2:处理中 3:已处理 4:处理失败")
	@TableField(exist = false)
	private Integer weeklyExpirePerformanceStatus;

	/** 周到期小区业绩重算跳过/失败原因 */
	@ApiModelProperty(value = "周到期小区业绩重算跳过/失败原因")
	@TableField(exist = false)
	private String weeklyExpirePerformanceSkipReason;

	/** 周到期小区业绩重算处理时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "周到期小区业绩重算处理时间")
	@TableField(exist = false)
	private Date weeklyExpirePerformanceTime;

	/** G7团队新增统计状态 0未处理 1已处理 */
	@ApiModelProperty(value = "G7团队新增统计状态 0未处理 1已处理")
	private Integer g7NewPerformanceStatus;

	/** G7团队新增统计处理时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "G7团队新增统计处理时间")
	private Date g7NewPerformanceTime;

	/** G7团队到期统计状态 0未处理 1已处理 */
	@ApiModelProperty(value = "G7团队到期统计状态 0未处理 1已处理")
	private Integer g7ExpirePerformanceStatus;

	/** G7团队到期统计处理时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "G7团队到期统计处理时间")
	private Date g7ExpirePerformanceTime;

	private Integer createDay;
}
