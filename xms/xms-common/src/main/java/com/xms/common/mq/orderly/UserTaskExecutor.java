package com.xms.common.mq.orderly;

import cn.hutool.core.util.IdUtil;
import com.xms.common.thread.ExecutorKit;

import java.util.concurrent.ExecutorService;

/**
 * @author: GT63S
 * @createDate: 2024/9/9
 */

public class UserTaskExecutor {
	private final UserTaskQueue taskQueue;
	private final ExecutorService virtualThreadExecutor;
	private final TaskProcessorManager processorManager;

	public UserTaskExecutor(UserTaskQueue taskQueue, TaskProcessorManager processorManager) {
		this.taskQueue = taskQueue;
		this.processorManager = processorManager;
		this.virtualThreadExecutor = ExecutorKit.newVirtualExecutor("virtual-thread" + IdUtil.getSnowflakeNextIdStr());
	}

	public void start() {
		virtualThreadExecutor.execute(() -> {
			while (true) {
				Task task = taskQueue.fetchTask();
				if (task != null) {
					// 根据任务类型找到相应的处理器并处理任务
					TaskProcessor processor = processorManager.getProcessor(task.getType());
					if (processor != null) {
						processor.processTask(task);
					}
				} else {
					Thread.onSpinWait();
				}
			}
		});
	}

	public void submitTask(Task task) {
		taskQueue.submitTask(task);
	}
}

