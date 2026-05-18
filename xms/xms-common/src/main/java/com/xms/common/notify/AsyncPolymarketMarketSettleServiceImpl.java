package com.xms.common.notify;

import cn.hutool.core.util.StrUtil;
import com.xms.common.config.redis.delayqueue.RedissonDelayHandler;
import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * Polymarket市场结算延迟队列派发实现。
 *
 * <p>该服务只负责把已抢占为“结算中”的marketSlug投递到统一Redisson延迟队列，不查询Gamma API、不处理订单、
 * 不写钱包。真正发奖仍由消费者调用processSettlingMarket复核后完成。</p>
 */
@Service
@Slf4j
@AllArgsConstructor
public class AsyncPolymarketMarketSettleServiceImpl implements AsyncPolymarketMarketSettleService {

	private final RedissonDelayHandler redissonDelayHandler;

	/**
	 * 投递Polymarket市场结算消息到统一延迟队列。
	 *
	 * <p>消息使用marketSlug作为orderId，bizType使用SysConstant.THIRTY，消费者到期后只会触发市场级结算入口。
	 * 发送失败会触发Spring Retry；达到最大重试次数后进入recover日志。</p>
	 *
	 * @param marketSlug Polymarket市场slug
	 */
	@Override
	@Retryable(maxAttemptsExpression = "${xms.stream.maxAttempts}", backoff = @Backoff(delayExpression = "${xms.stream.backOffInitialInterval}",
		multiplierExpression = "${xms.stream.backOffMultiplier}"))
	public void sendMarketSettleMessage(String marketSlug) {
		if (StrUtil.isBlank(marketSlug)) {
			log.warn("Polymarket市场结算消息缺少marketSlug");
			throw new ServiceException(ResponseCode.CODE_1294);
		}
		String slug = marketSlug.trim();
		// 延迟1秒进入消费者，避免派发方事务刚提交时消费者立即读到未稳定数据；消费者仍会以数据库市场状态为准。
		RedissonDelayOrder<String> order = new RedissonDelayOrder<>(slug, SysConstant.ONE_LONG, SysConstant.THIRTY, slug,
			RedisConstant.StreamMsgConstant.DELAY_ORDER_TIMEOUT_QUEUE);
		redissonDelayHandler.add(order);
		log.info("Polymarket市场结算消息投递成功，marketSlug={}", slug);
	}

	/**
	 * Polymarket市场结算消息投递达到最大重试次数后的兜底日志。
	 *
	 * <p>这里不直接发奖、不修改市场状态；后续可通过Quartz再次扫描待结算市场，或后台人工检查结算中市场。</p>
	 *
	 * @param e 最终失败异常
	 * @param marketSlug Polymarket市场slug
	 */
	@Recover
	public void recover(Exception e, String marketSlug) {
		log.error("Polymarket市场结算消息投递达到最大重试次数，marketSlug={}", marketSlug, e);
		throw new ServiceException(ResponseCode.CODE_1296);
	}
}
