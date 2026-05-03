package com.xms.app.plugin.disruptor.Consumer;

import com.alibaba.fastjson.JSONObject;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.WorkHandler;
import com.xms.app.plugin.disruptor.ObjectEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description: 消费者抽奖类
 *
 */
@Slf4j
public abstract class AbsDisruptorConsumer<T> implements EventHandler<ObjectEvent<T>>, WorkHandler<ObjectEvent<T>> {
	public AbsDisruptorConsumer() {
	}


	/**
	 * EventHandler的方法
	 * 用于EventHandlerGroup， 独立消费  （同一个事件消息对于每个消费者都可以独立消费） ==>>>handleEventsWith
	 *
	 * @param event      时间对象
	 * @param sequence   消息序列号，
	 * @param endOfBatch 标记，以指示这是否是批处理中的最后一个事件,最后一个变成true
	 * @throws Exception
	 */
	@Override
	public void onEvent(ObjectEvent<T> event, long sequence, boolean endOfBatch) throws Exception {
		this.onEvent(event);
	}

	/**
	 * WorkHandler的方法
	 * 用于WorkPool   不重复消费 （同一事件消息只有一个消费者可消费,也就是，如果c0消费了消息m，则c1不再消费消息m） ==>>>handleEventsWithWorkerPool
	 *
	 * @param event
	 * @throws Exception
	 */
	@Override
	public void onEvent(ObjectEvent<T> event) throws Exception {
		this.consume(event.getObj());
	}

	/**
	 * 消费回调
	 *
	 * @param msg 消息体
	 */
	public abstract void consume(T msg) throws Exception;
}

