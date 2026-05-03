package com.xms.common.core.domain;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class StatisticsDataResp implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 3 社区
	 */
	private String ThreeNum;
	/**
	 * 2 生态
	 */
	private String TwoNum;
	/**
	 * 1 事业合伙人
	 */
	private String OneNum;
	/**
	 * 3 认购
	 */
	private String ThreeRengouNum;
	/**
	 * 2 认购
	 */
	private String TwoRengouNum;
	/**
	 * 1 认购
	 */
	private String OneRengouNum;
	/**
	 * 余额usdt
	 */
	private String balabceUsdtAmount;
	/**
	 * 余额moa
	 */
	private String balabceMoaAmount;
	/**
	 * 余额mo
	 */
	private String balabceMoAmount;
	/**
	 * 复利mo
	 */
	private String reMoAmount;
	/**
	 * 消费mA
	 */
	private String payMaAmount;
	/**
	 * 消费m0
	 */
	private String payMoAmount;
	/**
	 * usdt 充值
	 */
	private String usdtNum;
	/**
	 * mo充值
	 */
	private String moNum;
	/**
	 * moa充值
	 */
	private String moaNum;
	/**
	 * usdt 充值 今日
	 */
	private String usdtToNum;
	/**
	 * mo充值 今日
	 */
	private String moToNum;
	/**
	 * moa充值 jr
	 */
	private String moaToNum;
	/**
	 * usdt 提现
	 */
	private String usdtTxNum;
	/**
	 * mo 提现
	 */
	private String moTxNum;
	/**
	 * moa 提现
	 */
	private String moaTxNum;
	/**
	 * usdt 提现 今日
	 */
	private String usdtTxToNum;
	/**
	 * mo 提现 今日
	 */
	private String moTxToNum;
	/**
	 * moa 提现  今日
	 */
	private String moaTxToNum;
	/**
	 * ma消费
	 */
	private String maPayNum;
	/**
	 * 分红池  今日
	 */
	private String todayRedPoolNum;
	/**
	 * 分红池 总剩余
	 */
	private String allLastRedPoolNum;
	/**
	 * 全网当日产出MO（闪兑+动静态）
	 */
	private String todayTotalMO;
	/**
	 * (2)全网当日产出MOA（动态+挖矿）
	 */
	private String todayTotalMoA;
	/**
	 * (3)全网当日合成MA（两种闪兑）
	 */
	private String todayTotalMA;
	/**
	 * 销毁MOA数量
	 * 2、MOA使用途径：铸造资产包，OTC交易手续费，提现手续费，兑换MA
	 */
	private String destroyTotalMoA;


}
