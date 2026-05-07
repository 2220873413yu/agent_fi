package com.xms.common.notify;

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

@Service
@Slf4j
@AllArgsConstructor
public class AsyncTelegramMessageServiceImpl implements AsyncTelegramMessageService {

	private final RenegadeStreamTemplate streamTemplate;

	@Override
	@Retryable(maxAttemptsExpression = "${xms.stream.maxAttempts}", backoff = @Backoff(delayExpression = "${xms.stream.backOffInitialInterval}",
		multiplierExpression = "${xms.stream.backOffMultiplier}"))
	public void sendMessage(TelegramMessageDTO telegramMessageDTO) {
		// 统一写入 Telegram Stream，和业务线程解耦。
		RecordId res = streamTemplate.send(RedisConstant.StreamMsgConstant.TELEGRAM_MESSAGE, IdUtil.getSnowflakeNextIdStr(), JsonUtil.toJsonAsBytes(telegramMessageDTO));
		if (res == null || Func.isAllEmpty(res.getTimestamp())) {
			log.error("Telegram message enqueue failed dto:{}", telegramMessageDTO);
			throw new ServiceException("Telegram message enqueue failed");
		}
	}

	@Recover
	public void recover(Exception e, TelegramMessageDTO telegramMessageDTO) {
		log.error("Telegram message enqueue reached max retries dto:{}", telegramMessageDTO, e);
	}
}
