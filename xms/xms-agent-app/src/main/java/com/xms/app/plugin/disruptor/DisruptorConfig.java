package com.xms.app.plugin.disruptor;

import com.xms.app.plugin.disruptor.Consumer.MyConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 高性能无锁内存队列，适合各种任务编排，只需自定义对应的bean生产者就欧克了，
 * 注意：一定要在MyDisposableBean类中，把定义的bean生产 关闭 shutdown
 *
 * @author MIER
 */
@Component
public class DisruptorConfig {


	/**
	 * 一对一单一消费者
	 *
	 * @return
	 */
	@Bean("singleConsumer")
	public DisruptorProducer initDisruptorQueueSingle() {
		return DisruptorQueueFactory.getWorkPoolQueue(1024 * 1024,
			false, new MyConsumer("singleConsumer"));
	}

	/**
	 * 一生产 多消费者，同一条消息只能被一个消费者消费
	 *
	 * @return
	 */
	// @Bean("singleProMulConsumer")
	public DisruptorProducer initDisruptorQueueMulCons() {
		MyConsumer[] str = {new MyConsumer("---->singleConsumer-1"), new MyConsumer("---->singleConsumer-2")};
		return DisruptorQueueFactory.getWorkPoolQueue(1024 * 1024,
			false, str);
	}

	/**
	 * 订阅主题，指定消费者  一条消息，多个消费者独立消费，
	 *
	 * @return
	 */
	// @Bean("mulConsumer")
	public DisruptorProducer initDisruptorQueueTopic() {
		// 创建一个Disruptor队列操作类对象（RingBuffer大小为4，false表示只有一个生产者）
		// 创建一个消费者
		MyConsumer myConsumer = new MyConsumer("---->独立消费者1");
		MyConsumer myConsumer1 = new MyConsumer("---->独立消费者2");
		MyConsumer[] str = {myConsumer, myConsumer1};
		return DisruptorQueueFactory.getHandleEventsQueue(1024 * 1024,
			false, str);
	}

	/**
	 * 订阅主题，指定消费者 多个生产者
	 * 多生产者的时候要将 isMoreProducer 参数设置为 true，即 ProducerType 使用 ProducerType.MULTI。否则会出现数据丢失的情况。
	 */
	// @Bean("mulProducer")
	public DisruptorProducer initDisruptorQueueMul() {
		//这个后续可写死
		MyConsumer myConsumer = new MyConsumer("---->多个生产者的消费者");
		return DisruptorQueueFactory.getHandleEventsQueue(1024 * 1024,
			true, myConsumer);
	}
}
