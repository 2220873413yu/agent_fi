package com.xms.common.config.redis.delayqueue.config;

import com.xms.common.config.redis.delayqueue.handler.IsolationStrategy;
import com.xms.common.config.redis.delayqueue.message.MessageConverter;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @author GT63S
 */
public class RedissonQueue {

	/**
	 * 队列名称
	 */
    @Getter
    private final String queue;
	/**
	 * 是否延迟队列
	 */
	@Getter
    private final boolean delay;
	/**
	 * 默认null ，代表无需隔离
	 *  隔离策略主要是应用于如下场景：
	 * 在服务集群模式中，假设A、B、C三台机器，A机器生产的消息只希望自己消费，这就叫做隔离，\
	 * 可通过指定隔离策略进行集群的隔离，源码提供了DefaultIsolationStrategy，可根据需要使用。\
	 * 消息转换器主要是对消息进行转换以及一些附加处理如增加消息头等
	 */
	@Getter
    private final IsolationStrategy isolationHandler;
	/**
	 * 消息转换器
	 */
    @Getter
    private final MessageConverter messageConverter;

    public RedissonQueue(String queue) {
        this(queue, false);
    }

    public RedissonQueue(String queue, boolean delay) {
        this(queue, delay, null);
    }

    public RedissonQueue(String queue, boolean delay, IsolationStrategy isolationHandler) {
        this(queue, delay, isolationHandler, null);
    }

    public RedissonQueue(String queue, boolean delay, IsolationStrategy isolationHandler, MessageConverter messageConverter) {
        Assert.hasText(queue, "queue name must not be empty");
        this.queue = queue;
        this.delay = delay;
        this.isolationHandler = isolationHandler;
        this.messageConverter = messageConverter;
    }

    @Override
    public int hashCode() {
        return this.queue.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
            RedissonQueue that = (    RedissonQueue) o;
        return Objects.equals(this.queue, that.queue);
    }

}
