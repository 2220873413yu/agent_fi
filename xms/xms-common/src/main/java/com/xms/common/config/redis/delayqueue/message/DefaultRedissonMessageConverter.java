package com.xms.common.config.redis.delayqueue.message;

import com.xms.common.utils.uuid.IDUtils;

import java.util.Map;

/**
 * @author GT63S
 */
public class DefaultRedissonMessageConverter implements MessageConverter {

	@Override
	public QueueMessage<?> toMessage(Object payload, Map<String, Object> headers) {
		headers.put(RedissonHeaders.MESSAGE_ID, IDUtils.getSnowflakeStr());
		return QueueMessageBuilder.withPayload(payload).headers(headers).build();
	}

	@Override
	public Object fromMessage(RedissonMessage redissonMessage) throws MessageConversionException {
		return new String(redissonMessage.getPayload());
	}

}
