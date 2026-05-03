package com.xms.common.config.redis.delayqueue.message;

import org.springframework.messaging.Message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author GT63S
 */
public interface BatchMessageConverter extends MessageConverter {

    @Override
    default QueueMessage<?> toMessage(Object payload, Map<String, Object> headers) throws MessageConversionException {
        throw new UnsupportedOperationException("please see [toListMessage] method");
    }

    @Override
    default Object fromMessage(RedissonMessage redissonMessage) throws MessageConversionException {
        return this.fromMessage(Collections.singletonList(redissonMessage));
    }

    List<QueueMessage<?>> toListMessage(Object payload, Map<String, Object> headers);

    Message<?> fromMessage(List<RedissonMessage> redissonMessages) throws MessageConversionException;

}
