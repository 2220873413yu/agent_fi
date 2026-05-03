package com.xms.common.mq.orderly;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: GT63S
 * @createDate: 2024/9/9
 */
public class TaskProcessorManager {
	private final Map<TaskType, TaskProcessor> processorMap = new HashMap<>();

	public TaskProcessorManager() {
		// 注册不同类型的任务处理器
		processorMap.put(TaskType.TYPE_A, new TaskTypeAProcessor());
		processorMap.put(TaskType.TYPE_B, new TaskTypeBProcessor());
		// 添加其他任务处理器...
	}

	public TaskProcessor getProcessor(TaskType type) {
		return processorMap.get(type);
	}
}
