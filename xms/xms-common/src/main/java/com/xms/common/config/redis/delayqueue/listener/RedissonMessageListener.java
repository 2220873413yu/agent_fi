package com.xms.common.config.redis.delayqueue.listener;


/**
 * @author GT63S
 */
public interface RedissonMessageListener<T> {

    /**
     * on message method
     *
     * @param t message object
     * @throws Exception when consumer error
     */
    void onMessage(T t) throws Exception;

}
