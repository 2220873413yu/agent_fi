package com.xms.common.mq.orderly;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: GT63S
 * @createDate: 2024/9/9
 */
@Slf4j
public class TaskTypeAProcessor implements TaskProcessor {
	@Override
	public void processTask(Task task) {
		// 处理 TaskTypeA 逻辑
		// System.out.println("Processing TaskTypeB for user: " + task.getUserId()+"data: " + task.getData());
		log.debug("data：{}", task.getData());
	}
}



// 可以继续添加不同的任务处理器

