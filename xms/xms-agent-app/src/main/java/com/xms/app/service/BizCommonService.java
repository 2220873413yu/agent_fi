package com.xms.app.service;

import java.math.BigDecimal;

public interface BizCommonService {

	/**
	 * 获取dfc价格
	 * @return
	 */
	public BigDecimal getDFcPrice();

	/**
	 * 获取oort价格
	 * @return
	 */
	public BigDecimal getOortPrice();

	/**
	 * 获取AFI价格
	 * @return
	 */
	public BigDecimal getAfiPrice();
}
