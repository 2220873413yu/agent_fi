package com.xms.app.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 基金总收益dto对象
 * @author xms
 * @date 2023/10/07
 */
@Data
public class TotalEarningsDto {

	/**
	 * 活期基金:活期总收益/固定矿机的时候:累计收益/体验式基金:累计收益
	 */
	private BigDecimal totalEarnings;

	/**
	 * 活期基金:当前已释放/体验式基金:当前已释放
	 */
	private BigDecimal currentEarnings;

	/**
	 * 活期矿机:待释放/固定基金:剩余未释放
	 */
	private BigDecimal remainingEarnings;

	/**
	 * 活期基金:剩余未赎回本金/固定基金:剩余未赎回本金/体验式基金:剩余未赎回本金
	 */
	private BigDecimal unredeemedPrincipal;


	/**
	 * 固定基金:可领取收益
	 */
	private BigDecimal totalAwaitingAmount;

	/**
	 *  固定基金:最大可领取收益
	 */
	private BigDecimal maxAwaitingAmount;
}
