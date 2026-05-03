package com.xms.dao.entity.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 销毁订单列表记录
 * @author xms
 * @date 2023/10/07
 */
@Data
public class DestroyOrderDto {

	private Long id;

	/** 矿机天数 */
	private Long days;

	/** 价值多少u */
	private BigDecimal usdtValue;

	/** 销毁了多少个boomai */
	private BigDecimal validNum1Value;

	/** 已经获取了多少个boomai */
	private BigDecimal haveValidNum1;

	/** 订单状态 0:待支付,1:运行中,2:暂停,3:订单关闭了 */
	private Integer status;

	/** 支付时间 */
	private Date createTime;

	/** 是否已经加速 0:否,1:是 */
	private Integer isReduced;
}
