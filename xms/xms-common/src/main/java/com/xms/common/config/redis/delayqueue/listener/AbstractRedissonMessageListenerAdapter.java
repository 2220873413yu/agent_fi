package com.xms.common.config.redis.delayqueue.listener;




import com.xms.common.config.redis.delayqueue.message.*;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * @author GT63S
 */
public abstract class AbstractRedissonMessageListenerAdapter<T> implements RedissonMessageListener<T> {

    protected static class SimpleMessageConverter implements MessageConverter {

        @Override
        public QueueMessage<?> toMessage(Object payload, Map<String, Object> headers) throws MessageConversionException {
            return null;
        }

        @Override
        public String fromMessage(RedissonMessage redissonMessage) throws MessageConversionException {
            String charset = (String) redissonMessage.getHeaders().getOrDefault(RedissonHeaders.CHARSET_NAME, StandardCharsets.UTF_8.name());
            return new String(redissonMessage.getPayload(), Charset.forName(charset));
        }
    }

}
