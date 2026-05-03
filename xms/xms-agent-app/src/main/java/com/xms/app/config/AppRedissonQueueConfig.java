package com.xms.app.config;

import com.xms.common.config.redis.delayqueue.config.RedissonQueue;
import com.xms.common.constant.RedisConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 应用模块Redisson队列配置
 */
@Configuration
public class AppRedissonQueueConfig {

    /**
     * 注册订单超时延时队列
     */
    @Bean
    public RedissonQueue orderTimeoutQueue() {
        return new RedissonQueue(RedisConstant.StreamMsgConstant.DELAY_ORDER_TIMEOUT_QUEUE, true, null, null);
    }
} 