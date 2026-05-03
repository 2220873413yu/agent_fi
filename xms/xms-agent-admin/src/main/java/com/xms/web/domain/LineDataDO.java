package com.xms.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author: GT63S
 * @createDate: 2024/7/8
 */
@AllArgsConstructor
@Data
@NoArgsConstructor
public class LineDataDO {
	/**
	 * x 轴
	 */
	private String xkey;
	/**
	 * y 轴，int类型
	 */
	private Integer expectedData = 0;
	/**
	 * y轴， bigdecimal版
	 */
	private BigDecimal expectedRewardData = BigDecimal.ZERO;
	private BigDecimal expectedRewardDataV2 = BigDecimal.ZERO;

}
