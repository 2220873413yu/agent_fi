package com.xms.dao.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import com.xms.common.core.domain.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 后台托管订单列表DTO。
 *
 * <p>后台列表和导出使用该对象承载展示字段，避免直接返回托管订单数据库实体；
 * 同时补充 AFI 质押比例和加速倍率快照，便于查看订单是否加速以及加速口径。</p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StakeHostingOrderListDto extends BaseEntity {
	private static final long serialVersionUID = 1L;

	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	@Excel(name = "托管订单号", sort = 2, width = 30)
	@ApiModelProperty(value = "托管订单号")
	private String orderNo;

	@Excel(name = "用户ID", sort = 3)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@Excel(name = "钱包地址", sort = 4, width = 40)
	@ApiModelProperty(value = "用户钱包地址")
	private String account;

	@ApiModelProperty(value = "套餐ID")
	private Long packageId;

	@Excel(name = "套餐名称", sort = 5)
	@ApiModelProperty(value = "套餐名称快照")
	private String packageName;

	@Excel(name = "套餐天数", sort = 6)
	@ApiModelProperty(value = "套餐天数快照")
	private Integer packageDays;

	@Excel(name = "托管USDT金额", sort = 7)
	@ApiModelProperty(value = "托管USDT金额")
	private BigDecimal stakeUsdtAmount;

	@Excel(name = "服务费比例", sort = 8)
	@ApiModelProperty(value = "服务费比例快照，单位%")
	private BigDecimal serviceFeeRatio;

	@Excel(name = "业绩积分系数", sort = 8)
	@ApiModelProperty(value = "业绩积分系数快照")
	private BigDecimal performanceCoefficient;

	@Excel(name = "业绩积分", sort = 9)
	@ApiModelProperty(value = "业绩积分快照")
	private BigDecimal performancePoints;

	@Excel(name = "AFI质押比例", sort = 10)
	@ApiModelProperty(value = "AFI质押比例，单位%")
	private BigDecimal afiPledgeRatio;

	@Excel(name = "AFI加速倍率", sort = 11)
	@ApiModelProperty(value = "AFI加速倍率，例如1.10")
	private BigDecimal afiAccelerateRate;

	@Excel(name = "订单来源", sort = 12, dictType = "t_stake_hosting_order_source_type")
	@ApiModelProperty(value = "订单来源 0:用户购买 1:后台拨付")
	private Integer sourceType;

	@Excel(name = "支付状态", sort = 13, dictType = "t_stake_hosting_order_pay_status")
	@ApiModelProperty(value = "支付状态 0:待支付 1:支付成功")
	private Integer payStatus;

	@Excel(name = "业务状态", sort = 14, dictType = "t_stake_hosting_order_status")
	@ApiModelProperty(value = "业务状态 0:未开始 1:产出中 2:已完成 3:已暂停")
	private Integer status;

	@Excel(name = "支付hash", sort = 15, width = 40)
	@ApiModelProperty(value = "支付hash")
	private String payHash;

	@Excel(name = "链上支付金额", sort = 16)
	@ApiModelProperty(value = "链上支付金额")
	private BigDecimal payAmount;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "支付时间", sort = 17, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date payTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "生效时间", sort = 18, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date effectiveTime;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "完成时间", sort = 19, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date finishTime;

	@Excel(name = "已运行天数", sort = 20)
	@ApiModelProperty(value = "已运行天数，即已发放静态收益次数")
	private Integer runDays;

	@Excel(name = "今日静态收益", sort = 21)
	@ApiModelProperty(value = "今日静态收益")
	private BigDecimal todayReward;

	@Excel(name = "累计静态收益", sort = 22)
	@ApiModelProperty(value = "累计已发静态收益")
	private BigDecimal totalStaticReward;

	@Excel(name = "是否回本", sort = 23, dictType = "t_stake_hosting_order_return_principal")
	@ApiModelProperty(value = "是否回本 0:否 1:是")
	private Integer isReturnPrincipal;

	@Excel(name = "AFI加速", sort = 24, dictType = "t_stake_hosting_order_afi_accelerated")
	@ApiModelProperty(value = "是否已绑定AFI加速 0:否 1:是")
	private Integer afiAccelerated;

	@Excel(name = "最近发放日期", sort = 25)
	@ApiModelProperty(value = "最近一次发放日期，格式yyyyMMdd")
	private Integer lastRewardDay;

	private Long performanceStartTime;

	private Long performanceEndTime;

	private Integer weeklyPerformanceStatus;

	private String weeklyPerformanceSkipReason;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date weeklyPerformanceTime;

	private Integer createDay;
}
