package com.xms.common.config.redis.delayqueue.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author GT63S
 */
public class RedissonQueueRegistry {

    private Map<String, QueueRegistryInfo> registryInfoContainer = new ConcurrentHashMap<>(8);

    protected void registerQueueInfo(String queueName, QueueRegistryInfo queueInfo) {
        if (queueInfo == null) {
            return;
        }
        this.registryInfoContainer.put(queueName, queueInfo);
    }

    public QueueRegistryInfo getRegistryInfo(String queueName) {
       return this.registryInfoContainer.get(queueName);
    }

}
