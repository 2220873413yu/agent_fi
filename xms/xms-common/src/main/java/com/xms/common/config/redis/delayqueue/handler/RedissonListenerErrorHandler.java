package com.xms.common.config.redis.delayqueue.handler;

import com.xms.common.config.redis.delayqueue.message.RedissonMessage;
import org.springframework.messaging.Message;

/**
 * @author GT63S
 */
@FunctionalInterface
public interface RedissonListenerErrorHandler {

    /**
     * error handler
     *
     * @param message          redisson message
     * @param messagingMessage spring message
     * @param throwable        throwable
     */
    void handleError(RedissonMessage message, Message<?> messagingMessage, Throwable throwable);

}
