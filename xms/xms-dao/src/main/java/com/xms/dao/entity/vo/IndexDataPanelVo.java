package com.xms.dao.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页数据页面看板
 */
@Data
public class IndexDataPanelVo {



	/**
	 *   4.当日新增有效用户
	 */
	//private Long v3;

	/**
	 *   4. 全网有效用户
	 */
	//private Long v4;

	/**
	 * 质押OORT订单量
	 */
	private BigDecimal v6 = BigDecimal.ZERO;

	/**
	 * 质押OORT总金额
	 */
	private BigDecimal v11 = BigDecimal.ZERO;
	/**
	 * 锁仓总量
	 */
	private BigDecimal v12 = BigDecimal.ZERO;

	/**
	 * 已释放总量
	 */
	private BigDecimal v13 = BigDecimal.ZERO;

	/**
	 * 获取用户总节点数量
	 */
	private BigDecimal v7 = BigDecimal.ZERO;

	/**
	 * 全网质押节点量
	 */
	private BigDecimal v8 = BigDecimal.ZERO;
	/**
	 * 质押中的订单
	 */
	private BigDecimal v9 = BigDecimal.ZERO;

	/**
	 *
	 */
	private BigDecimal v10 = BigDecimal.ZERO;













	/**
	 * 今日全网静态
	 */
	//private BigDecimal v14 = BigDecimal.ZERO;
	/**
	 * 全网静态
	 */
	//private BigDecimal v24 = BigDecimal.ZERO;

	/**
	 * 今日全网动态
	 */
	//private BigDecimal v15 = BigDecimal.ZERO;
	/**
	 * 全网动态
	 */
	//private BigDecimal v25 = BigDecimal.ZERO;

	/**
	 * 当前全网未领取利息
	 */
	//private BigDecimal v16 = BigDecimal.ZERO;

	/**
	 * 当前全网未领取余额宝
	 */
	//private BigDecimal v17 = BigDecimal.ZERO;


	/**
	 * 今日提现USDT
	 */
	private BigDecimal v18 = BigDecimal.ZERO;;

	/**
	 * 今日提现BPAY
	 */
	private BigDecimal v19 = BigDecimal.ZERO;;

	/**
	 * 历史提现 USDT
	 */
	private BigDecimal v20 = BigDecimal.ZERO;;

	/**
	 * 历史提现 BPAY
	 */
	private BigDecimal v21 = BigDecimal.ZERO;


	/**
	 * 总计充值(MAI)
	 */
	private BigDecimal v22 = BigDecimal.ZERO;


	/**
	 * 今日充值(MAI)
	 */
	private BigDecimal v23 = BigDecimal.ZERO;

	/**
	 * 全网USDT
	 */
	private BigDecimal v26 = BigDecimal.ZERO;

	/**
	 * 全网FTN
	 */
	private BigDecimal v27 = BigDecimal.ZERO;

	/**
	 * v28
	 */
	private BigDecimal v28 = BigDecimal.ZERO;

	/**
	 * 区代理人数
	 */
	private BigDecimal v29 = BigDecimal.ZERO;

	/**
	 * 县代理人数
	 */
	private BigDecimal v30 = BigDecimal.ZERO;

	/**
	 * 省代理人数
	 */
	private BigDecimal v31 = BigDecimal.ZERO;



	/**
	 * 累计提现USDT
	 */
	private BigDecimal v32 = BigDecimal.ZERO;

	/**
	 * 累计提现AFI
	 */
	private BigDecimal v33 = BigDecimal.ZERO;

	/**
	 * 累计提现OORT
	 */
	private BigDecimal v34 = BigDecimal.ZERO;

	/**
	 * 累计提现DFC
	 */
	private BigDecimal v35 = BigDecimal.ZERO;


	/**
	 * 累计转账USDT
	 */
	private BigDecimal v36 = BigDecimal.ZERO;

	/**
	 * 累计转账DFC
	 */
	private BigDecimal v37 = BigDecimal.ZERO;

	/**
	 * 累计转账OORT
	 */
	private BigDecimal v38 = BigDecimal.ZERO;

	/**
	 * 累计转账锁定USDT
	 */
	private BigDecimal v39 = BigDecimal.ZERO;
	/**
	 * 累计产出DFC
	 */
	private BigDecimal v40 = BigDecimal.ZERO;
}
