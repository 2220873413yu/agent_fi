package com.xms.common.config.redis.delayqueue.config;

/**
 * @author GT63S
 */
public enum ListenerType {
    /**
     * take one message once
     */
    SIMPLE,
    /**
     * take list message once
     */
    BATCH
}
