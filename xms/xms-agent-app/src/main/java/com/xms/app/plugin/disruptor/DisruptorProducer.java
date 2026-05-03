package com.xms.app.plugin.disruptor;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

import java.util.Iterator;
import java.util.List;

/**
 * 生产者发送消息
 *
 * @Description: Disruptor 队列操作工具类 DisruptorQueue，用于初始化 disruptor 以及 ringBuffer 对象，并封装类一些常用的方法：
 */

public class DisruptorProducer<T> {
	private Disruptor<ObjectEvent<T>> disruptor;
	private RingBuffer<ObjectEvent<T>> ringBuffer;

	public DisruptorProducer(Disruptor<ObjectEvent<T>> disruptor) {
		this.disruptor = disruptor;
		this.ringBuffer = disruptor.getRingBuffer();
		this.disruptor.start();
	}

	/**
	 * 发布事件里 我草拼命的塞(单一）
	 *
	 * @param t
	 */
	public void add(T t) throws Exception {
		if (t != null) {
			long sequence = this.ringBuffer.next();
			try {
				ObjectEvent<T> event = this.ringBuffer.get(sequence);
				event.setObj(t);
			} finally {
				this.ringBuffer.publish(sequence);
			}
		}
	}

	/**
	 * 拼命的塞list
	 *
	 * @param ts
	 */
	public void addAll(List<T> ts) throws Exception {
		if (ts != null) {
			for (T t : ts) {
				if (t != null) {
					this.add(t);
				}
			}
		}
	}

	public long cursor() {
		//消息放在缓冲区哪个元素位置 位置角标
		return this.disruptor.getRingBuffer().getCursor();
	}

	/**
	 * 关闭 1. 生产者的生产线程必须执行在disruptor.shutdown方法之前。
	 * 生产者 == disruptor.shutdown ==消费者
	 * 2. disruptor.shutdown方法必须执行在所有消费者线程启动之前。
	 */
	public void shutdown() {
		//当缓冲区元素index没有，也就是小于0 才能关闭，否则会
		this.disruptor.shutdown();
	}

	public Disruptor<ObjectEvent<T>> getDisruptor() {
		return this.disruptor;
	}

	public void setDisruptor(Disruptor<ObjectEvent<T>> disruptor) {
		this.disruptor = disruptor;
	}

	public RingBuffer<ObjectEvent<T>> getRingBuffer() {
		return this.ringBuffer;
	}

	public void setRingBuffer(RingBuffer<ObjectEvent<T>> ringBuffer) {
		this.ringBuffer = ringBuffer;
	}
}
