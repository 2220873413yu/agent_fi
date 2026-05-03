package com.xms.common.mq.orderly;

import lombok.Getter;

/**
 * @author: GT63S
 * @createDate: 2024/9/9
 */
@Getter
public class Task {
	private final String userId;
	/**
	 * 任务类型
	 */
	private final TaskType type;
	private final Object data;  // 任务相关的数据

	public Task(String userId, TaskType type, Object data) {
		this.userId = userId;
		this.type = type;
		this.data = data;
	}

}


