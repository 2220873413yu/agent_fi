package com.xms.app.service;

import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.app.entity.dto.StakeHostingAfiAccelerateConfigDto;
import com.xms.app.entity.dto.StakeHostingAfiPledgeDto;
import com.xms.app.entity.dto.StakeHostingOrderDto;
import com.xms.app.entity.dto.StakeHostingPackageDto;
import com.xms.app.entity.resp.CreateStakeHostingOrderResp;
import com.xms.app.entity.vo.CreateStakeHostingOrderVo;
import com.xms.app.entity.vo.PledgeStakeHostingAfiVo;
import com.xms.common.core.domain.api.ResultPista;

import java.util.List;

/**
 * 托管业务Service
 */
public interface BizStakeHostingService {
	/**
	 * 查询 App 托管套餐列表。
	 *
	 * 只返回前端展示和下单需要的套餐字段，不直接暴露数据库对象。
	 *
	 * @return 已上架托管套餐展示列表
	 */
	List<StakeHostingPackageDto> packageList();

	/**
	 * 创建托管订单
	 */
	ResultPista<CreateStakeHostingOrderResp> createOrder(CreateStakeHostingOrderVo req, Long userId);

	/**
	 * 查询我的托管订单列表。
	 *
	 * @param lastId 上一页最后一条订单ID，空表示第一页
	 * @param status 业务状态，空表示全部状态
	 * @return 当前登录用户托管订单展示列表
	 */
	List<StakeHostingOrderDto> orderList(Long lastId, Integer status);

	/**
	 * 查询可提交 AFI 质押加速的托管订单列表。
	 *
	 * @param lastId 上一页最后一条订单ID，空表示第一页
	 * @return 当前登录用户可加速订单展示列表
	 */
	List<StakeHostingOrderDto> accelerateOrderList(Long lastId);

	/**
	 * 查询 AFI 质押加速配置套餐。
	 *
	 * @return 已启用 AFI 加速配置展示列表
	 */
	List<StakeHostingAfiAccelerateConfigDto> afiAccelerateConfigList();

	/**
	 * 查询托管订单详情。
	 *
	 * @param id 托管订单ID
	 * @return 当前登录用户托管订单详情展示对象
	 */
	StakeHostingOrderDto orderDetail(Long id);

	/**
	 * 提交AFI质押加速
	 */
	ResultPista<StakeHostingAfiPledgeDto> pledgeAfi(PledgeStakeHostingAfiVo req);

	/**
	 * 托管订单链上支付回调
	 */
	ResultPista<String> orderCallback(StakeOrderBo req);
}
