package com.xms.dao.entity.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 奖金记录BO对象
 *
 * @author xms
 * @date 2025-11-19
 */
@Data
public class RewardRecordBo {

	/**
	 * 订单编号
	 */
	private String orderCode;

	/**
	 * 金额
	 */
	private BigDecimal amount;

	/**
	 * 创建时间
	 */
	private Date createTime;

	/**
	 * 来源类型
	 */
	private Integer sourceType;
}
