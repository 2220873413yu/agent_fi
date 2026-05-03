package com.xms.app.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 平台币价格dto对象
 */
@Data
public class PtbPriceDto {

	/**
	 * 价格日期 格式为:20250809 年月日
	 */
	private Long date;

	/**
	 * 当日价格
	 */
	private BigDecimal price;
}
