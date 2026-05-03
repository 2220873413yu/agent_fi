package com.xms.common.config.redis.delayqueue.listener;

import com.xms.common.config.redis.delayqueue.config.ListenerType;
import com.xms.common.config.redis.delayqueue.handler.IsolationStrategy;
import com.xms.common.config.redis.delayqueue.handler.RedissonListenerErrorHandler;
import com.xms.common.config.redis.delayqueue.message.MessageConverter;
import lombok.Data;

/**
 * @author GT63S
 */
@Data
public class ContainerProperties {

    private String queue;

    private ListenerType listenerType;

    private RedissonListenerErrorHandler errorHandler;

    private IsolationStrategy isolationStrategy;

    private MessageConverter messageConverter;

    private int concurrency;

    private int maxFetch;

}
