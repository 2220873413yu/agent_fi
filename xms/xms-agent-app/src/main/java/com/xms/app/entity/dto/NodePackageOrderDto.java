package com.xms.app.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 节点订单购买记录
 */
@Data
public class NodePackageOrderDto {
	/** 订单号 */
	private String orderNo;
	/** 支付hash */
	private String hash;
	/** 下单时节点等级快照 */
	private Integer packageLevel;
	/** 下单时直推奖励比例快照(%) */
	private BigDecimal directReferralRate;
	/** 下单时间推奖励比例快照(%)，无则NULL */
	private BigDecimal indirectReferralRate;
	/** 下单时权重系数快照(倍数) */
	private BigDecimal weightMultiplier;
	/** 减免比例 */
	private BigDecimal predOrderFeeReliefRate;
	/** 支付金额 */
	private BigDecimal orderValueUsdt;
	/** 支付时间 */
	private Date payTime;
	/**
	 * 创建时间
	 */
	private Date createTime;
}
