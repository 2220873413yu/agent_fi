package com.xms.web.domain;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ReleaseMiningBo {
	private BigDecimal reward;
	private Long id;
	/**
	 * 最后一次结算的时候FSN价格
	 */
	private BigDecimal closePrice;
}
