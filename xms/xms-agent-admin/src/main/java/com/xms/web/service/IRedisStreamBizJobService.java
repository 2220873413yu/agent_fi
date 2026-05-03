package com.xms.web.service;

import java.util.List;

public interface IRedisStreamBizJobService {

	/**
	 * 质押订单的处理
	 * @param list
	 * @return
	 */
	Integer handlerDynamicOrderSettlement(List list);
}
