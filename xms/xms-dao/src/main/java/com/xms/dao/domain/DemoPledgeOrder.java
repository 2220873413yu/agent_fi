package com.xms.dao.domain;

import com.baomidou.mybatisplus.annotation.IdType;
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
 * 示例质押订单对象 t_demo_pledge_order。
 *
 * <p>订单用于演示先落库状态、扣款、投递Redis、消费者抢占处理业绩的标准链路。</p>
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "t_demo_pledge_order")
public class DemoPledgeOrder extends BaseEntity {
	private static final long serialVersionUID = 1L;
	public static final int STATUS_PENDING_PAY = 0;
	public static final int STATUS_PAID = 1;
	public static final int STATUS_PROCESSING = 2;
	public static final int STATUS_COMPLETED = 3;
	public static final int STATUS_FAILED = 4;
	public static final int REWARD_STATUS_RELEASING = 0;
	public static final int REWARD_STATUS_FINISHED = 1;

	/** 主键ID */
	@TableId(type = IdType.AUTO)
	@Excel(name = "主键ID", sort = 1)
	@ApiModelProperty(value = "主键ID")
	private Long id;

	/** 订单号 */
	@Excel(name = "订单号", sort = 2, width = 30)
	@ApiModelProperty(value = "订单号")
	private String orderNo;

	/** 用户ID */
	@Excel(name = "用户ID", sort = 3)
	@ApiModelProperty(value = "用户ID")
	private Long userId;

	/** 套餐ID */
	@Excel(name = "套餐ID", sort = 4)
	@ApiModelProperty(value = "套餐ID")
	private Long packageId;

	/** 套餐名称快照 */
	@Excel(name = "套餐名称", sort = 5)
	@ApiModelProperty(value = "套餐名称快照")
	private String packageName;

	/** 质押USDT金额 */
	@Excel(name = "质押USDT金额", sort = 6)
	@ApiModelProperty(value = "质押USDT金额")
	private BigDecimal pledgeUsdtAmount;

	/** 释放天数快照 */
	@Excel(name = "释放天数", sort = 7)
	@ApiModelProperty(value = "释放天数快照")
	private Integer releaseDays;

	/** 日利率快照，单位% */
	@Excel(name = "日利率(%)", sort = 8)
	@ApiModelProperty(value = "日利率快照，单位%")
	private BigDecimal dailyRate;

	/** 已释放天数 */
	@Excel(name = "已释放天数", sort = 9)
	@ApiModelProperty(value = "已释放天数")
	private Integer releasedDays;

	/** 收益释放状态 0:释放中 1:已完成 */
	@Excel(name = "收益释放状态", sort = 10, dictType = "t_demo_pledge_reward_status")
	@ApiModelProperty(value = "收益释放状态 0:释放中 1:已完成")
	private Integer rewardStatus;

	/** 累计收益USDT金额 */
	@Excel(name = "累计收益USDT", sort = 11)
	@ApiModelProperty(value = "累计收益USDT金额")
	private BigDecimal totalRewardUsdtAmount;

	/** 最近释放日期，yyyyMMdd */
	@Excel(name = "最近释放日期", sort = 12)
	@ApiModelProperty(value = "最近释放日期，yyyyMMdd")
	private Integer lastRewardDay;

	/** 最近释放时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "最近释放时间", sort = 13, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	@ApiModelProperty(value = "最近释放时间")
	private Date lastRewardTime;

	/** 状态 0:待支付 1:已支付 2:处理中 3:已完成 4:失败 */
	@Excel(name = "状态", sort = 14, dictType = "t_demo_pledge_order_status")
	@ApiModelProperty(value = "状态 0:待支付 1:已支付 2:处理中 3:已完成 4:失败")
	private Integer status;

	/** 支付时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "支付时间", sort = 15, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date payTime;

	/** 开始处理时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "开始处理时间", sort = 16, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date processTime;

	/** 完成时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@Excel(name = "完成时间", sort = 17, width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
	private Date finishTime;

	/** 失败原因 */
	@Excel(name = "失败原因", sort = 18, width = 40)
	@ApiModelProperty(value = "失败原因")
	private String failReason;
}
