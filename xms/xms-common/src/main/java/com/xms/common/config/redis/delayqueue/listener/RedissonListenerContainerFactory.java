package com.xms.common.config.redis.delayqueue.listener;

/**
 * @author GT63S
 */
public interface RedissonListenerContainerFactory {

    RedissonListenerContainer createListenerContainer(ContainerProperties containerProperties);

}
