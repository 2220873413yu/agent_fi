package com.xms.app.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MiningPackageVo {
	/** id */
	private Long id;
	/** 套餐名称 */
	private String name;
	/** 奖励倍数 */
	private BigDecimal rewardMultiplier;
	/** 折扣 */
	private BigDecimal discountPercentage;
	/** 赠送手续费率 */
	private BigDecimal feeRatio;
	/** 图片 */
	private String img;
	/** 销量 */
	private Integer buyNum;
	/** 剩余天数 */
	private Integer days;
	/** 最少购买金额限制 */
	private BigDecimal minBuyPrice;
}
