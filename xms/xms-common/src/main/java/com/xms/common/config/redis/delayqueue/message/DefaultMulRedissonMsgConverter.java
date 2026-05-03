package com.xms.common.config.redis.delayqueue.message;

import com.alibaba.fastjson2.JSONObject;
import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
import com.xms.common.utils.uuid.IDUtils;

import java.util.Map;

/**
 * 默认多级延迟的转换器
 *
 * @author GT63S
 */
public class DefaultMulRedissonMsgConverter implements MessageConverter {

	@Override
	public QueueMessage<?> toMessage(Object payload, Map<String, Object> headers) {
		headers.put(RedissonHeaders.MESSAGE_ID, IDUtils.getSnowflakeStr());
		return QueueMessageBuilder.withPayload(payload).headers(headers).build();
	}

	@Override
	public Object fromMessage(RedissonMessage redissonMessage) throws MessageConversionException {
		byte[] payload = redissonMessage.getPayload();
		String payloadStr = new String(payload);
		return JSONObject.parseObject(payloadStr, RedissonDelayOrder.class);
	}

}
