package com.xms.app.plugin.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.xms.app.plugin.disruptor.Consumer.AbsDisruptorConsumer;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 实例化方法
 *
 * @author MIER
 */
public class DisruptorQueueFactory {

	public DisruptorQueueFactory() {
	}


	/**
	 * BlockingWaitStrategy 使用ReentrantLock，失败则进入等待队列等待唤醒重试。当吞吐量和低延迟不如CPU资源重要时使用。 默认  常用,最节约CPU的
	 * SleepingWaitStrategy 尝试200次 。前100次直接重试，后100次每次失败后调用Thread.yield()让出CPU，全失败线程睡眠（默认100纳秒 ）。常用，性能和CPU使用之间均衡
	 * YieldingWaitStrategy 自旋N次，让出CPU，该策略将使用100%的CPU，如果其他线程请求CPU资源，这种策略更容易让出CPU资源。 低延迟，推荐这个，通俗点讲，没人用CPU，就独占，有用的线程，就直接让出，会表现服务器CPU爆满
	 *
	 * @param queueSize      一定得设置2的幂次方
	 * @param isMoreProducer 是否多生产者
	 * @param consumers      指定消费者
	 * @param <T>
	 * @return
	 */
	public static <T> DisruptorProducer<T> getWorkPoolQueue(int queueSize, boolean isMoreProducer,
															AbsDisruptorConsumer<T>... consumers) {
		Disruptor<ObjectEvent<T>> disruptor = new Disruptor<ObjectEvent<T>>(new ObjectEventFactory<>(),
			queueSize, Executors.defaultThreadFactory(),
			isMoreProducer ? ProducerType.MULTI : ProducerType.SINGLE,
			new SleepingWaitStrategy());
		//Group的消费者对于同一条消息m不重复消费
		disruptor.handleEventsWithWorkerPool(consumers);
		return new DisruptorProducer<>(disruptor);
	}


	/**
	 * BlockingWaitStrategy 使用ReentrantLock，失败则进入等待队列等待唤醒重试。当吞吐量和低延迟不如CPU资源重要时使用。 默认  常用,最节约CPU的
	 * YieldingWaitStrategy 自旋N次，让出CPU，该策略将使用100%的CPU，如果其他线程请求CPU资源，这种策略更容易让出CPU资源。 低延迟，推荐这个，通俗点讲，没人用CPU，就独占，有用的线程，就直接让出，会表现服务器CPU爆满
	 * SleepingWaitStrategy 尝试200次 。前100次直接重试，后100次每次失败后调用Thread.yield()让出CPU，全失败线程睡眠（默认100纳秒 ）。常用
	 * DummyWaitStrategy：返回的Sequence值为0，正常环境是用不上的
	 * BusySpinWaitStrategy  线程一直自旋等待，比较耗CPU。最好是将线程绑定到特定的CPU核心上使用。 JDK9之下慎用（最好别用）
	 * LiteBlockingWaitStrategy 与BlockingWaitStrategy类似，区别在增加了原子变量signalNeeded，如果两个线程同时分别访问waitFor()和signalAllWhenBlocking()，可以减少ReentrantLock加锁次数。 但作者没有测试充分
	 * LiteTimeoutBlockingWaitStrategy  与LiteBlockingWaitStrategy类似，区别在于设置了阻塞时间，超过时间后抛异常。 但作者没有测试充分
	 * TimeoutBlockingWaitStrategy   与BlockingWaitStrategy类似，区别在于设置了阻塞时间，超过时间后抛异常。 但作者没有测试充分
	 * PhasedBackoffWaitStrategy 根据时间参数和传入的等待策略来决定使用哪种等待策略。当吞吐量和低延迟不如CPU资源重要时，可以使用此策略。
	 *
	 * @param queueSize      一定得设置2的幂次方
	 * @param isMoreProducer 是否多生产者
	 * @param consumers      指定消费者
	 * @param <T>
	 * @return
	 */
	public static <T> DisruptorProducer<T> getHandleEventsQueue(int queueSize, boolean isMoreProducer,
																AbsDisruptorConsumer<T>... consumers) {

		Disruptor<ObjectEvent<T>> disruptor = new Disruptor<ObjectEvent<T>>(new ObjectEventFactory<>(),
			queueSize, Executors.defaultThreadFactory(),
			isMoreProducer ? ProducerType.MULTI : ProducerType.SINGLE,
			new SleepingWaitStrategy());
		//关键点 Group中的每个消费者都会对m进行消费，各个消费者之间不存在竞争
		disruptor.handleEventsWith(consumers);
		return new DisruptorProducer<>(disruptor);
	}


	/**
	 * 直接通过传入的 Disruptor 对象创建操作队列（如果消费者有依赖关系的话可以用此方法）
	 *
	 * @param disruptor
	 * @param <T>
	 * @return
	 */
	public static <T> DisruptorProducer<T> getQueue(Disruptor<ObjectEvent<T>> disruptor) {
		return new DisruptorProducer<>(disruptor);
	}

}
