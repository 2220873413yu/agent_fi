package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.entity.dto.StakeHostingOrderListDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管订单Service接口
 *
 * @author xms
 */
public interface IStakeHostingOrderService extends XmsDataService<StakeHostingOrder> {
	/**
	 * 查询托管订单列表
	 *
	 * @param stakeHostingOrder 托管订单
	 * @return 托管订单集合
	 */
	List<StakeHostingOrder> selectStakeHostingOrderList(StakeHostingOrder stakeHostingOrder);

	/**
	 * 查询后台托管订单列表展示数据。
	 *
	 * @param query 查询条件
	 * @return 托管订单列表，包含AFI质押比例和加速倍率展示字段
	 */
	List<StakeHostingOrderListDto> selectStakeHostingOrderDtoList(StakeHostingOrderListDto query);

	/**
	 * 用户创建链上待支付托管订单
	 *
	 * @param userId 用户ID
	 * @param packageId 套餐ID
	 * @param amount 托管USDT金额
	 * @return 托管订单
	 */
	StakeHostingOrder createUserOrder(Long userId, Long packageId, BigDecimal amount);

	/**
	 * 链上支付回调确认托管订单
	 *
	 * @param orderNo 订单号
	 * @param payHash 支付hash
	 * @param payAmount 支付金额
	 * @return 处理结果
	 */
	int confirmChainPaid(String orderNo, String payHash, BigDecimal payAmount);

	/**
	 * 后台拨付托管订单，创建即产出中
	 *
	 * @param req 拨付参数
	 * @return 处理结果
	 */
	int createAdminGrantOrder(StakeHostingOrder req);

	/**
	 * 扣减托管业绩，订单完成时调用
	 *
	 * @param userId 用户ID
	 * @param amount 扣减金额
	 */
	void subtractHostingPerformance(Long userId, BigDecimal amount);

	/**
	 * 扣减托管业绩，订单完成时调用
	 *
	 * @param userId 用户ID
	 * @param amount 扣减金额
	 * @param orderId 托管订单ID
	 */
	void subtractHostingPerformance(Long userId, BigDecimal amount, Long orderId);
}
