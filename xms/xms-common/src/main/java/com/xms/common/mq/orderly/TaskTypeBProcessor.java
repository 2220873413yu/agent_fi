package com.xms.common.mq.orderly;

/**
 * @author: GT63S
 * @createDate: 2024/9/9
 */
public class TaskTypeBProcessor implements TaskProcessor {
	@Override
	public void processTask(Task task) {
		// 处理 TaskTypeB 逻辑
		System.out.println("Processing TaskTypeB for user: " + task.getUserId()+"data: " + task.getData());
	}
}
