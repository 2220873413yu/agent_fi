package com.xms.app.entity.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页币种价格信息
 * @author xms
 * @date 2023/10/23
 */
@Data
public class CoinInfoBo {

	/**
	 * boomai价格
	 */
	private BigDecimal boomaiPrice;

	/**
	 * mai价格
	 */
	private BigDecimal maiPrice;

	/**
	 * boomai价格涨跌幅
	 */
	private BigDecimal boomaiChangeRate;

	/**
	 * mai价格涨跌幅
	 */
	private BigDecimal maiChangeRate;
}
