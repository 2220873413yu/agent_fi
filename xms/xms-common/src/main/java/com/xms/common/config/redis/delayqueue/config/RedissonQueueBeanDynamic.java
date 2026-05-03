package com.xms.common.config.redis.delayqueue.config;

import com.xms.common.config.redis.delayqueue.handler.IsolationStrategy;
import com.xms.common.config.redis.delayqueue.message.FastJsonCodec;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

/**
 * @author GT63S
 */
@Component
@Slf4j
public class RedissonQueueBeanDynamic implements BeanFactoryAware {

	private BeanFactory beanFactory;

	private RedissonClient redissonClient;

	private RedissonQueueRegistry redissonQueueRegistry;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
		this.redissonClient = this.beanFactory.getBean(RedissonClient.class);
		this.redissonQueueRegistry = this.beanFactory.getBean(RedissonConfigUtils.REDISSON_QUEUE_REGISTRY_BEAN_NAME, RedissonQueueRegistry.class);
	}

	public void postProcessAfterInitialization(RedissonQueue redissonQueue) throws BeansException {
		final QueueRegistryInfo registryInfo = new QueueRegistryInfo();
		final String queueName = redissonQueue.getQueue();
		final QueueRegistryInfo registeredInfo = this.redissonQueueRegistry.getRegistryInfo(queueName);
		if (registeredInfo != null) {
			log.debug("duplicate bean of RedissonQueue named [ {} ]", queueName);
			return;
		}
		final IsolationStrategy isolationHandler = redissonQueue.getIsolationHandler();
		final String isolatedName = isolationHandler == null ? queueName : isolationHandler.getRedisQueueName(queueName);
		final RBlockingQueue<Object> blockingQueue = this.redissonClient.getBlockingQueue(isolatedName, FastJsonCodec.INSTANCE);
		RDelayedQueue<Object> delayedQueue = null;
		if (redissonQueue.isDelay()) {
			delayedQueue = this.redissonClient.getDelayedQueue(blockingQueue);
		}
		registryInfo.setQueueName(queueName);
		registryInfo.setIsolatedName(isolatedName);
		registryInfo.setQueue(redissonQueue);
		registryInfo.setIsolationHandler(isolationHandler);
		registryInfo.setMessageConverter(redissonQueue.getMessageConverter());
		registryInfo.setBlockingQueue(blockingQueue);
		registryInfo.setDelayedQueue(delayedQueue);
		this.redissonQueueRegistry.registerQueueInfo(queueName, registryInfo);
	}

}
