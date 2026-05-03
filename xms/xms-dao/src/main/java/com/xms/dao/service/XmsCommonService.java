package com.xms.dao.service;

import com.xms.common.core.domain.api.ResultPista;

/**
 * @author: renengadePISTA
 */
public interface XmsCommonService {

	/**
	 * 系统结算时间，不允许交易
	 * @return
	 */
	ResultPista checkMineSettleTime();


}
