package com.xms.app.plugin.disruptor.Consumer;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@Slf4j
public class MyConsumer extends AbsDisruptorConsumer<Object> {
	/**
	 * 消费者名称
	 */
	private final String name;

	public MyConsumer(String name) {
		this.name = name;
	}

	@Override
	public void consume(Object data) {
		//处理业务逻辑
		log.info("消费者：{}，拿到队列中的数据：{}", this.name, JSONObject.toJSONString(data));
	}
}

