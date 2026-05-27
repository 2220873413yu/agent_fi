package com.xms.dao.example;

import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.dao.service.impl.MqSendTemplate;

/**
 * 复杂订单异步后置处理示例。
 *
 * <p>适用于节点订单、质押订单、团队奖励、等级重算等耗时业务。
 * Redis 消息只作为触发器，数据库订单状态才是事实来源。</p>
 */
public class AsyncOrderFlowExample {
	private static final int STATUS_PENDING = 0;
	private static final int STATUS_PROCESSING = 1;
	private static final int STATUS_FINISHED = 2;
	private static final int STATUS_FAILED = 3;

	/**
	 * 主流程创建订单后，在事务提交后投递 Redis Stream 消息。
	 *
	 * <p>{@link MqSendTemplate#syncSendSync(String, Object)} 内部会注册 afterCommit，
	 * 因此消费者不会读到未提交或已回滚的订单。真实业务应为新业务定义独立 streamName，
	 * 不要把所有耗时业务都塞进同一个动态订单队列。</p>
	 *
	 * @param orderRepository 订单落库入口
	 * @param mqSendTemplate Redis Stream 发送模板
	 * @param userId 下单用户 ID
	 * @param bizType 后置处理业务类型
	 * @param streamName 当前业务独立 Redis Stream 名称
	 * @return 新创建的订单 ID
	 */
	public Long createOrderAndSendAfterCommit(OrderRepository orderRepository, MqSendTemplate mqSendTemplate,
											  Long userId, Integer bizType, String streamName) {
		Long orderId = orderRepository.insertPendingOrder(userId, STATUS_PENDING);

		OrderMsgDO msg = new OrderMsgDO();
		msg.setId(orderId);
		msg.setBizType(bizType);
		mqSendTemplate.syncSendSync(streamName, msg);
		return orderId;
	}

	/**
	 * 消费者按订单 ID 重新查库、抢占状态并处理后置业务。
	 *
	 * @param orderRepository 订单查询和状态更新入口
	 * @param rewardProcessor 后置奖励或业绩处理入口
	 * @param msg Redis Stream 消息体，只信任其中的最小触发字段
	 */
	public void consumeOrderMessage(OrderRepository orderRepository, RewardProcessor rewardProcessor, OrderMsgDO msg) {
		Long orderId = msg.getId();
		BusinessOrder order = orderRepository.findById(orderId);
		if (order == null || order.status() != STATUS_PENDING) {
			return;
		}

		// 消费前先做状态抢占，避免多个消费者重复发奖或重复改业绩。
		boolean claimed = orderRepository.compareAndSetStatus(orderId, STATUS_PENDING, STATUS_PROCESSING);
		if (!claimed) {
			return;
		}

		try {
			rewardProcessor.process(orderId, msg.getBizType());
			orderRepository.updateStatus(orderId, STATUS_FINISHED, null);
		} catch (Exception ex) {
			orderRepository.updateStatus(orderId, STATUS_FAILED, ex.getMessage());
			throw ex;
		}
	}

	/**
	 * 示例订单仓储接口，真实业务应替换为对应 Service 或 Mapper。
	 */
	public interface OrderRepository {
		Long insertPendingOrder(Long userId, Integer status);

		BusinessOrder findById(Long orderId);

		boolean compareAndSetStatus(Long orderId, Integer fromStatus, Integer toStatus);

		void updateStatus(Long orderId, Integer status, String failReason);
	}

	/**
	 * 示例后置处理接口，真实业务中通常包含奖励、业绩、等级或结算逻辑。
	 */
	public interface RewardProcessor {
		void process(Long orderId, Integer bizType);
	}

	/**
	 * 示例订单快照，只保留异步消费所需的关键字段。
	 *
	 * @param id 订单 ID
	 * @param userId 用户 ID
	 * @param status 订单状态
	 */
	public record BusinessOrder(Long id, Long userId, Integer status) {
	}
}
