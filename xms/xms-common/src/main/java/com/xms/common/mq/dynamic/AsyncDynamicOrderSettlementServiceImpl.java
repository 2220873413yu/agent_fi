package com.xms.common.mq.dynamic;

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

import java.util.List;

/**
 * 挖矿订单之后的用户升级处理
 */
@Service
@Slf4j
@AllArgsConstructor
public class AsyncDynamicOrderSettlementServiceImpl implements AsyncDynamicOrderSettlementService {
	private final RenegadeStreamTemplate streamTemplate;

	/**
	 * 接收
	 *
	 * @param orderMsgDOList 需要处理的订单
	 */
	@Override
	@Retryable(maxAttemptsExpression = "${xms.stream.maxAttempts}", backoff = @Backoff(delayExpression = "${xms.stream.backOffInitialInterval}",
		multiplierExpression = "${xms.stream.backOffMultiplier}"))
	public void sendMessage(List<OrderMsgDO> orderMsgDOList) {
		log.debug("质押的订单处理:{}", orderMsgDOList);
		RecordId res = streamTemplate.send(RedisConstant.StreamMsgConstant.ORDER_DYNAMIC_SETTLEMENT, IdUtil.getSnowflakeNextIdStr(), JsonUtil.toJsonAsBytes(orderMsgDOList));
		if (res == null || Func.isAllEmpty(res.getTimestamp())) {
			log.error("质押的订单处理处理更新失败");
			throw new ServiceException("质押的订单处理更新失败");
		}
	}

	@Override
	@Retryable(maxAttemptsExpression = "${xms.stream.maxAttempts}", backoff = @Backoff(delayExpression = "${xms.stream.backOffInitialInterval}",
		multiplierExpression = "${xms.stream.backOffMultiplier}"))
	public void sendMessage(UserPerformanceUpdateVO performanceUpdateVO) {
		log.debug("跨链分发之后的订单处理:{}", performanceUpdateVO);
		RecordId res = streamTemplate.send(RedisConstant.StreamMsgConstant.ORDER_DYNAMIC_SETTLEMENT, IdUtil.getSnowflakeNextIdStr(), JsonUtil.toJsonAsBytes(performanceUpdateVO));
		if (res == null || Func.isAllEmpty(res.getTimestamp())) {
			log.error("跨链分发之后的订单处理更新失败");
			throw new ServiceException("跨链分发之后的订单处理更新失败");
		}
	}

	/**
	 * 动态订单的奖励结算结算，等定时任务处理
	 *
	 * @param e
	 */
	@Recover
	public void recover(Exception e, List<Long> list) {
		//补偿有定时任务处理
		/*Map<String, Object> map = new HashMap<>();
		map.put("date", DateUtils.getDate());
		map.put("type", "104");
		map.put("content", JsonUtil.toJsonAsBytes(list));
		taskMapper.addAdRewardErrorEvent(map);*/
		log.error("人工介入处理{}", list);
	}
}
