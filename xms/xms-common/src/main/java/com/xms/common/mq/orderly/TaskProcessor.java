package com.xms.common.mq.orderly;

/**
 * @author: GT63S
 * @createDate: 2024/9/9
 */
@FunctionalInterface
public interface TaskProcessor {
	void processTask(Task task);
}
