package com.xms.common.config.redis.delayqueue.config;

import cn.hutool.core.collection.ListUtil;
import com.xms.common.config.redis.delayqueue.MultiDelayMsgDO;
import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
import com.xms.common.config.redis.delayqueue.message.DefaultRedissonMessageConverter;
import com.xms.common.config.redis.delayqueue.message.MessageConverter;
import com.xms.common.config.redis.delayqueue.message.QueueMessage;
import com.xms.common.config.redis.delayqueue.message.RedissonHeaders;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redisson 队列发送的干活
 * @author GT63S
 */
@Slf4j
public class RedissonTemplate implements BeanFactoryAware, SmartInitializingSingleton {

    private BeanFactory beanFactory;

    private     RedissonQueueRegistry redissonQueueRegistry;
    @Getter
    private MessageConverter globalMessageConverter = new DefaultRedissonMessageConverter();

    public void setGlobalMessageConverter(MessageConverter globalMessageConverter) {
        Assert.notNull(globalMessageConverter, "MessageConverter must not be null");
        this.globalMessageConverter = globalMessageConverter;
    }

    public void send(final String queueName, final Object payload) {
        this.send(queueName, payload, new HashMap<>(8));
    }

    public void send(final String queueName, final Object payload, Map<String, Object> headers) {
        this.checkQueueAndPayload(queueName, payload);
        final QueueRegistryInfo registryInfo = this.checkAndGetRegistryInfo(queueName);
        final RBlockingQueue<Object> blockingQueue = registryInfo.getBlockingQueue();
        final MessageConverter messageConverter = this.getRequiredMessageConverter(queueName);

        this.fillInfrastructureHeaders(queueName, headers);
        QueueMessage<?> message = messageConverter.toMessage(payload, headers);
        blockingQueue.offer(message);
    }

	/**
	 *  缓存专清
	 * @param key
	 */
	public void sendCleanCacheWithDelay(final String key) {
		MultiDelayMsgDO<String> delayMsg = new MultiDelayMsgDO<>(key, ListUtil.toList(2000L, 4000L));
		Long aLong = delayMsg.removeNextDelay();
		RedissonDelayOrder<MultiDelayMsgDO<String>> order = new RedissonDelayOrder<>(key, aLong, SysConstant.ONE, delayMsg
			, RedisConstant.StreamMsgConstant.DELAY_DEL_CACHE);
		this.sendWithDelay(RedisConstant.StreamMsgConstant.DELAY_DEL_CACHE, order, aLong);
	}
	/**
	 *
	 * @param queueName
	 * @param payload
	 * @param delay  毫秒级级别
	 */
    public void sendWithDelay(final String queueName, final Object payload, final long delay) {
        this.sendWithDelay(queueName, payload, new HashMap<>(8), delay);
    }

    public void sendWithDelay(final String queueName, final Object payload, Map<String, Object> headers, final long delay) {
        this.checkQueueAndPayload(queueName, payload);
        Assert.isTrue(delay > 0, "delay millis must be positive");
        final QueueRegistryInfo registryInfo = this.checkAndGetRegistryInfo(queueName);
        final RDelayedQueue<Object> delayedQueue = registryInfo.getDelayedQueue();
        Assert.notNull(delayedQueue, "the delay queue doesn't define");
        final MessageConverter messageConverter = this.getRequiredMessageConverter(queueName);

        this.fillInfrastructureHeaders(queueName, headers);
        headers.put(RedissonHeaders.EXPECTED_DELAY_MILLIS, delay);
        QueueMessage<?> message = messageConverter.toMessage(payload, headers);
        delayedQueue.offer(message, delay, TimeUnit.MILLISECONDS);
    }

    private void checkQueueAndPayload(String queueName, Object payload) {
        Assert.hasText(queueName, "queueName must not be empty");
        Assert.notNull(payload, "payload must not be null");
    }

    private QueueRegistryInfo checkAndGetRegistryInfo(String queueName) {
        QueueRegistryInfo registryInfo = this.redissonQueueRegistry.getRegistryInfo(queueName);
        Assert.notNull(registryInfo, "queue not registered");
        RBlockingQueue blockingQueue = registryInfo.getBlockingQueue();
        Assert.notNull(blockingQueue, "target queue doesn't define");
        return registryInfo;
    }

    private MessageConverter getRequiredMessageConverter(String queueName) {
        final QueueRegistryInfo registryInfo = this.redissonQueueRegistry.getRegistryInfo(queueName);
        MessageConverter messageConverter = registryInfo.getMessageConverter();
        if (messageConverter == null) {
            messageConverter = this.globalMessageConverter;
        }
        return messageConverter;
    }

    private void fillInfrastructureHeaders(final String queueName, final Map<String, Object> headers) {
        headers.put(RedissonHeaders.DELIVERY_QUEUE_NAME, queueName);
        headers.put(RedissonHeaders.SEND_TIMESTAMP, System.currentTimeMillis());
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.redissonQueueRegistry = this.beanFactory.getBean(RedissonConfigUtils.REDISSON_QUEUE_REGISTRY_BEAN_NAME,     RedissonQueueRegistry.class);
    }

}
