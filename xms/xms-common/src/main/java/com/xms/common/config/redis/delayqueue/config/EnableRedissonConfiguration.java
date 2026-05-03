package com.xms.common.config.redis.delayqueue.config;

import com.xms.common.config.redis.delayqueue.listener.RedissonListenerRegistry;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.context.annotation.Scope;

/**
 * @author GT63S
 */
@Configuration
public class EnableRedissonConfiguration {

    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean(name = RedissonConfigUtils.REDISSON_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
    public RedissonAnnotationBeanPostProcessor redissonAnnotationBeanPostProcessor() {
        return new RedissonAnnotationBeanPostProcessor();
    }

    @Scope(BeanDefinition.SCOPE_SINGLETON)
    @Bean(name = RedissonConfigUtils.REDISSON_LISTENER_REGISTRY_BEAN_NAME)
    public RedissonListenerRegistry redissonListenerRegistry() {
        return new RedissonListenerRegistry();
    }

}
