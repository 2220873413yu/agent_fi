package com.xms.common.thread;

import com.jd.platform.async.executor.Async;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * 确保应用退出时能关闭后台线程
 */
@Component
@Slf4j
public class ShutdownManager {
	private static final Logger logger = LoggerFactory.getLogger("sys-user");

	@PreDestroy
	public void destroy() {
		shutdownAsyncManager();
	}

	/**
	 * 停止异步执行任务
	 */
	private void shutdownAsyncManager() {
		try {
			logger.info("====关闭后台任务任务线程池====");
			AsyncManager.me().shutdown();
			//  BY RENEGADE PISTA: 2023/5/22   任务变排量的线程池要关闭
			Async.shutDown();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

}
