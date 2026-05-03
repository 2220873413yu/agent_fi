package com.xms.app.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * 债券购买记录
 * @author xms
 * @date 2023/07/06
 */
@Data
public class PackageOrderVo {
	/** id */
	private Long id;
	/** 订单号 */
	private String orderNo;
	/** 还剩下多少天*/
	private Integer days;
	/**
	 * 订单天数
	 */
	private Integer remainingDays;

	/** 折扣价值(PTB) */
	private BigDecimal baseAmount;

	/** 订单状态 0:释放中,1:已结束,2:暂停中 */
	private Integer status;

	/** 复利总额 折扣价值+总释放利息 */
	private BigDecimal totalCompoundInterest;

	/** 当前可领取利息(爆块奖励) */
	private BigDecimal availableCompoundInterest;

	/**
	 * 本次爆块奖励
	 */
	private BigDecimal currentReward;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 矿机开始释放利息时间 格式为:20250318
	 */
	private Integer openTime;
}
