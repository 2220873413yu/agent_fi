package com.xms.app.entity.vo;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 空投页面显示数据
 */
@Data
public class AirdropClaimPageInfoVo {




	/**
	 * 是否有开启的空投活动 true:代表有,false:代表没有
	 */
	private Boolean hasActiveAirdrop;


	/** 活动轮次编号 */
	private String roundNo;

	/**
	 * 用户当前可用的领取次数
	 */
	private Integer availableClaimCount;

	/**
	 * //是否能领取 true:代表可以领取,false:代表不可以领取
	 *  1:代表可以领取,2:代表领取过了,3:代表支付中
	 */
	private Integer hasClaim;

	/**
	 * 领取代币数量
	 */
	private BigDecimal tokenPerClaim;

	/**
	 * 剩余可领取次数
	 */
	private Long remainingQuota;

	/**
	 * 每次领取需支付的OKB数量（按USDT价值计算）
	 */
	private BigDecimal okbAmountByUsdtValue;

	/**
	 * 需要的激活币数量
	 */
	private BigDecimal activationCost;

	/**
	 * 0:未激活,1:已激活,2:激活中
	 */
	private Integer activationStatus;
}
