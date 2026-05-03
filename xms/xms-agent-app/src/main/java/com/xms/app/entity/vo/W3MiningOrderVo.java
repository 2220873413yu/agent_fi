package com.xms.app.entity.vo;

import com.xms.common.annotation.Excel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class W3MiningOrderVo {
	private Long id;

	/**
	 * 质押本金->质押FSN數量
	 */
	private BigDecimal fsnValue;

	/**
	 * 取本金额
	 */
	private BigDecimal haveFsnValue;

	/**
	 * 质押业绩->usdt数量
	 */
	private BigDecimal usdtValue;


	/**
	 * 今日利息
	 */
	private BigDecimal dayReward;

	/**
	 * 余额宝
	 */
	private BigDecimal validNum3;

	/**
	 * 创建时间
	 */
	private Date createTime;


	/**
	 * 固定期限矿机->日利率
	 */
	private BigDecimal ratio;

	/**
	 * 固定期限矿机->剩余产出动静态奖励
	 */
	private BigDecimal haveFsnMultipliedValue;

	/**
	 * 固定期限矿机->剩余天数
	 */
	private Integer haveDays;

	/**
	 * 状态 0:释放中,1:已经达到最大倍数,2:已结束
	 */
	@Excel(name = "状态")
	private Integer status;
}
