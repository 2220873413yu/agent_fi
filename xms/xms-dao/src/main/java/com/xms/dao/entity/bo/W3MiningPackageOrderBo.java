package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class W3MiningPackageOrderBo {

	/**
	 * id
	 */
	private Long id;
	/**
	 * 剩余可领取奖励
	 */
	private BigDecimal haveFsnMultipliedValue;
	/**
	 * 订单状态
	 */
	private Integer status;
	private Date updateTime;
}
