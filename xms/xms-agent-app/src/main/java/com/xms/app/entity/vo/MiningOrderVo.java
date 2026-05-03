package com.xms.app.entity.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class MiningOrderVo {
	/** 主键id */
	private Long id;
	/** 订单号 */
	private String orderNo;

	/** 总天数 */
	private Integer days;

	/** 剩余多少天 */
	private Integer haveDays;

	/**
	 * 现在是第几天
	 */
	private Integer currentDayInCycle;

	/** 矿机价值多少u(矿机额度) */
	private BigDecimal amount;

	/** 每天收益 */
	private BigDecimal dayReward;

	/** 订单状态 0:释放中,1:暂停,2:已结束 */
	private Integer status;



	/**
	 * 创建时间
	 */
	private Date createTime;


	/** 可领取矿机金额 */
	private BigDecimal availableAmount;

	/**
	 * 矿机余额(矿机价值-已经释放奖励)
	 */
	private BigDecimal miningAmount;


	/** 已领取总量(不领取不会加)领取就会增加 */
	private BigDecimal totalReleasedAmount;


	/** 释放总量(7天释放总量) */
	private BigDecimal releasedAmount;
}
