package com.xms.common.mq.orderly;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 试验阶段，还没正式投产
 *
 * @author: GT63S
 * @createDate: 2024/9/9
 */
@Component
public class TaskManager {
	//可以采用jctool的NonBlockingHashMap
	private final Map<String, UserTaskExecutor> userExecutors = new ConcurrentHashMap<>();
	private final TaskProcessorManager processorManager = new TaskProcessorManager();

	public void submitUserTask(String userId, TaskType type, Object data) {
		Task task = new Task(userId, type, data);
		UserTaskExecutor executor = userExecutors.computeIfAbsent(userId, id -> {
			UserTaskQueue queue = new UserTaskQueue();
			UserTaskExecutor userExecutor = new UserTaskExecutor(queue, processorManager);
			userExecutor.start();
			return userExecutor;
		});
		executor.submitTask(task);
	}

	// public static void main(String[] args) throws InterruptedException {
	// 	// 初始化 TaskManager 和 TaskProcessorManager
	// 	TaskManager taskManager = new TaskManager();
	//
	// 	// 创建并提交用户任务
	// 	String userId1 = "user1";
	// 	String userId2 = "user2";
	// 	for (int i = 0; i < 10000; i++) {
	// 		taskManager.submitUserTask(userId1, TaskType.TYPE_A, "Task A1 Data：：： " + i);
	// 		// taskManager.submitUserTask(userId1, TaskType.TYPE_B, "Task B1 Data：：： "+i);
	// 		// taskManager.submitUserTask(userId1, TaskType.TYPE_A, "Task A2 Data：：： "+i);
	// 	}
	// 	// 提交 User1 的任务
	// 	taskManager.submitUserTask(userId1, TaskType.TYPE_A, "Task A1 Data");
	// 	taskManager.submitUserTask(userId1, TaskType.TYPE_B, "Task B1 Data");
	// 	taskManager.submitUserTask(userId1, TaskType.TYPE_A, "Task A2 Data");
	//
	// 	// 提交 User2 的任务
	// 	taskManager.submitUserTask(userId2, TaskType.TYPE_B, "Task B2 Data");
	// 	taskManager.submitUserTask(userId2, TaskType.TYPE_A, "Task A3 Data");
	//
	// 	// 为了观察任务执行结果，主线程等待一段时间
	// 	Thread.sleep(2000);
	// }
}
