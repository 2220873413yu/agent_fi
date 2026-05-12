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

	/**
	 * 按用户当前未完成托管订单刷新有效用户状态。
	 *
	 * <p>101 任务会在本轮所有静态收益和团队奖励都处理完后统一调用，避免同一用户多笔订单同批次完成时提前判断。</p>
	 *
	 * @param userId 用户ID
	 */
	void refreshUserValidByUnfinishedHostingOrder(Long userId);

	/**
	 * 同步重算托管订单相关用户的小区业绩和真实等级。
	 *
	 * <p>Redis消费者会调用该方法执行实际重算；HTTP回调和后台拨付只负责发送队列消息。</p>
	 *
	 * @param orderId 用于定位下单用户及其上级链路的托管订单ID
	 */
	void recalculateStakeHostingLevel(Long orderId);

	/**
	 * 事务提交后发送托管等级重算消息。
	 *
	 * <p>等级重算会重算小区业绩并遍历上级链路，业务入口只发Redis队列消息，不同步执行耗时逻辑。</p>
	 *
	 * @param orderId 托管订单ID
	 */
	void sendStakeHostingLevelRecalculateAfterCommit(Long orderId);
}
