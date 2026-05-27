package com.xms.common.mq.demo;

import cn.hutool.core.util.IdUtil;
import com.xms.common.config.redis.stream.RenegadeStreamTemplate;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.Func;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dreamlu.mica.core.utils.JsonUtil;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * 示例质押订单后置处理消息生产者实现。
 */
@Service
@Slf4j
@AllArgsConstructor
public class AsyncDemoPledgeOrderServiceImpl implements AsyncDemoPledgeOrderService {
	private final RenegadeStreamTemplate streamTemplate;

	/**
	 * 将已扣款成功的示例质押订单投递到独立 Redis Stream。
	 *
	 * <p>这里只发送订单ID，消费者重新查库并按订单状态抢占处理，避免消息体成为业务事实来源。</p>
	 *
	 * @param orderId 示例质押订单ID
	 */
	@Override
	@Retryable(maxAttemptsExpression = "${xms.stream.maxAttempts}", backoff = @Backoff(delayExpression = "${xms.stream.backOffInitialInterval}",
		multiplierExpression = "${xms.stream.backOffMultiplier}"))
	public void sendMessage(Long orderId) {
		log.debug("示例质押订单后置处理投递，orderId={}", orderId);
		RecordId res = streamTemplate.send(RedisConstant.StreamMsgConstant.DEMO_PLEDGE_ORDER, IdUtil.getSnowflakeNextIdStr(), JsonUtil.toJsonAsBytes(orderId));
		if (res == null || Func.isAllEmpty(res.getTimestamp())) {
			log.error("示例质押订单后置处理投递失败，orderId={}", orderId);
			throw new ServiceException("示例质押订单后置处理投递失败");
		}
	}

	/**
	 * 投递重试耗尽后的兜底日志，订单仍以数据库状态为准，可由补偿任务重新投递或人工处理。
	 *
	 * @param e 投递异常
	 * @param orderId 示例质押订单ID
	 */
	@Recover
	public void recover(Exception e, Long orderId) {
		log.error("示例质押订单后置处理投递重试失败，orderId={}", orderId, e);
	}
}
