package com.xms.app.service;

import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.app.entity.resp.CreateStakeHostingOrderResp;
import com.xms.app.entity.vo.CreateStakeHostingOrderVo;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.domain.StakeHostingPackage;

import java.util.List;

/**
 * 托管业务Service
 */
public interface BizStakeHostingService {
	/**
	 * 托管套餐列表
	 */
	List<StakeHostingPackage> packageList();

	/**
	 * 创建托管订单
	 */
	ResultPista<CreateStakeHostingOrderResp> createOrder(CreateStakeHostingOrderVo req, Long userId);

	/**
	 * 我的托管订单
	 */
	List<StakeHostingOrder> orderList(Long lastId, Integer status);

	/**
	 * 托管订单链上支付回调
	 */
	ResultPista<String> orderCallback(StakeOrderBo req);
}
