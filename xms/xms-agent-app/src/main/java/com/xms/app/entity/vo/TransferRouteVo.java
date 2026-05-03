package com.xms.app.entity.vo;

import lombok.Data;

import java.math.BigDecimal;


/**
 * 获得分发方向的vo对象
 */
@Data
public class TransferRouteVo {

	/**
	 * 分发方向 1:TRC->BSC,2:BSC->TRC,3:暂无分发机会
	 */
	private Integer routeType;
	/**
	 * trx分发数量
	 */
	private BigDecimal trxAmount;

	/**
	 * bsc分发数量
	 */
	private BigDecimal bscAmount;
}
