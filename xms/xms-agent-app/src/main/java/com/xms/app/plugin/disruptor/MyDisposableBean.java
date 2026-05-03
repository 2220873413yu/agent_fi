package com.xms.app.plugin.disruptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @Description:
 *
 */
@Component
@Slf4j
public class MyDisposableBean implements DisposableBean {
	@Autowired
	private DisruptorProducer singleConsumer;
	// @Qualifier("mulConsumer")
	// @Autowired
	// private DisruptorProducer mulConsumer;
	// @Qualifier("mulProducer")
	// @Autowired
	// private DisruptorProducer mulProducer;

	/**
	 * 优雅的关闭所有哈哈
	 *
	 * @throws Exception
	 */
	@Override
	public void destroy() throws Exception {
		singleConsumer.shutdown();
		// mulConsumer.shutdown();
		// mulProducer.shutdown();
		log.info("======SB============终于傻逼试的优雅的结束了不容易");

	}
}
