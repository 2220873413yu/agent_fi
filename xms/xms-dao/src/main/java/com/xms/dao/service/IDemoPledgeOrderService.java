package com.xms.dao.service;

import com.xms.dao.domain.DemoPledgeOrder;
import com.xms.dao.entity.req.DemoPledgeBuyReq;

import java.util.List;

/**
 * 示例质押订单Service接口。
 */
public interface IDemoPledgeOrderService extends XmsDataService<DemoPledgeOrder> {
	/**
	 * 查询示例质押订单列表。
	 *
	 * @param demoPledgeOrder 查询条件
	 * @return 示例质押订单集合
	 */
	List<DemoPledgeOrder> selectDemoPledgeOrderList(DemoPledgeOrder demoPledgeOrder);

	/**
	 * 后台演示购买示例质押套餐。
	 *
	 * @param req 购买请求
	 * @return 已创建并支付成功的订单
	 */
	DemoPledgeOrder buy(DemoPledgeBuyReq req);
}
