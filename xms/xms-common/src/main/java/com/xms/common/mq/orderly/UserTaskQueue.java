package com.xms.common.mq.orderly;

import io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueue;

/**
 * @author: GT63S
 * @createDate: 2024/9/9
 */

class UserTaskQueue {
	private final MpscArrayQueue<Task> taskQueue = new MpscArrayQueue<>(1024);

	public void submitTask(Task task) {
		taskQueue.offer(task);
	}

	public Task fetchTask() {
		return taskQueue.poll();
	}
}
