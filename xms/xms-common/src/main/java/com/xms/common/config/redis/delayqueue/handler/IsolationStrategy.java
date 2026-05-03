package com.xms.common.config.redis.delayqueue.handler;

/**
 * @author GT63S
 */
public interface IsolationStrategy {

    /**
     * acquire queue name apply to redis
     *
     * @param queue queue name
     * @return actual queue name at redis server
     */
    String getRedisQueueName(String queue);

}
