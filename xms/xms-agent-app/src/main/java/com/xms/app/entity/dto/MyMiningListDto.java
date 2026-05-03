package com.xms.app.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 我的矿机列表
 * @author xms
 * @date 2023/10/07
 */
@Data
public class MyMiningListDto {
	/** 主键id */
	private Long id;

	/** 矿机编号 */
	private String miningNo;

	/** 订单编号 */
	private String orderNo;

	/** 矿机算力 */
	private BigDecimal computingPower;

	/** 创建时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	/** 质押时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date stakeDate;

	/** 状态 0:未质押,1:启动中,2:正在产币 */
	private Integer status;

	/** 质押类型 1:托管,2:自提 */
	private Integer stakeType;

	/** 累计收益 */
	private BigDecimal totalReward;

	/** 支付方式 1:USDT,2:DFC */
	private Integer payType;

	/** 订单价值多少usdt */
	private BigDecimal orderValueUsdt;

	/** 支付USDT金额 */
	private BigDecimal payValidNum1;
	/** 支付DFC金额 */
	private BigDecimal payValidNum2;
	/** 支付锁定USDT金额 */
	private BigDecimal payValidNum4;

	/** 今日产出 */
	private BigDecimal todayOutDfc;

	/**
	 * 是否发货 0:否,1:是
	 */
	private Integer shippingStatus;

	/**
	 * 物流公司名称
	 */
	private String shippingCompany;

	/**
	 * 物流单号
	 */
	private String trackingNo;



	/**
	 * 收件人信息
	 */
	private String remark;

}
