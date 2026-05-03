package com.xms.app.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xms
 * @date 2020/05/05
 */
@Data
public class CardUpgradeLogDto {

	private Long id;
	/**
	 * 卡片类型 1:普通卡,2:白银卡,3白金卡,4:黑金卡
	 */
	private Integer cardType;

	/**
	 * 本次支付金额
	 */
	private BigDecimal amountDelta;
	/**
	 * 获得算力
	 */
	private BigDecimal computingPower;

	/**
	 * 额外算力
	 */
	private BigDecimal extraComputingPower;



	/**
	 * 流水类型 1-购买 2-升级
	 */
	private Integer flowType;



	/**
	 * 业务类型 1:升级扣卡,2:升级加卡
	 */
	private Integer bizType;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 升级前扣的算力
	 */
	private BigDecimal fromPower;
	/**
	 * 升级后套餐算力
	 */
	private BigDecimal toPower;
	/**
	 * 升级后套餐额外算力
	 */
	private BigDecimal toExtraComputingPower;
}
