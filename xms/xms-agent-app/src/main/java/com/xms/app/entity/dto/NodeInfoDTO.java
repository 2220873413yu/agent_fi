package com.xms.app.entity.dto;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 节点信息
 * @author xms
 * @date 2023/6/12
 */
@Data
public class NodeInfoDTO {
	/** 节点价格 */
	private BigDecimal price;
	/** 等级 */
	private Integer level;
	/** 直推奖励比例(%) */
	private BigDecimal directReferralRate;
	/** 间推奖励比例(%)，无则0 */
	private BigDecimal indirectReferralRate;
	/** 权重系数(倍数) */
	private BigDecimal weightMultiplier;
	/** 预测下单手续费减免比例(%) */
	private BigDecimal predOrderFeeReliefRate;

	/**
	 * 节点分红比例
	 */
	private BigDecimal shareRatio;
}
