package com.xms.app.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.xms.common.annotation.Excel;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值记录dto对象
 * @author: liuya
 * @date: 2023/1/5
 */
@Data
public class RechargeRecordDto {
	/** 主键id */
	private String id;
	/** 用户ID */
	private Long userId;

	/** 充值订单号 */
	private String orderNo;

	/**
	 * 充值金额
	 */
	private BigDecimal rechargeAmount;

	/**
	 * 币种类型 1:USDT
	 */
	private Integer coinType;

	/**
	 * 交易hash
	 */
	private String txId;

	/**
	 * 钱包地址
	 */
	private String remark;
}
