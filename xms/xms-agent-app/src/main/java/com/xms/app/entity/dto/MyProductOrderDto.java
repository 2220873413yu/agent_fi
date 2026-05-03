package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品订单dto对象
 */
@Data
public class MyProductOrderDto {

	/**
	 * 商品快照信息
	 */
	private String productSnapshot;

	/**
	 * 发货状态 0待发货 1已发货
	 */
	private Integer shipStatus;

	/**
	 * 购买数量
	 */
	private Integer num;

	/**
	 * 发货时间
	 */
	private Date shipTime;

	/**
	 * 支付时间
	 */
	private Date payTime;

	/** 质押订单号 */
	private String orderNo;

	/**
	 * 收货信息JSON快照
	 */
	private String receiverInfo;

	/**
	 * 物流公司
	 */
	private String shipCompany;

	/**
	 * 物流单号
	 */
	private String shipNo;


	/** 质押金额/USDT单位 */
	private BigDecimal stakeUsdtAmount;
}
