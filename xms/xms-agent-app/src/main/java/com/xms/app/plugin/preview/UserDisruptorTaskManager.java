package com.xms.app.plugin.preview;

import cn.hutool.core.util.IdUtil;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.xms.common.thread.ExecutorKit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author: GT63S
 * @createDate: 2024/9/12
 */
public class UserDisruptorTaskManager {
	private final ExecutorService executor = ExecutorKit.newVirtualExecutor("disruptor-task-virtual:" + IdUtil.getSnowflakeNextIdStr());
	private final ConcurrentHashMap<String, Disruptor<UserTaskEvent>> userDisruptorMap = new ConcurrentHashMap<>();

	public static void main(String[] args) throws InterruptedException {
		UserDisruptorTaskManager manager = new UserDisruptorTaskManager();

		// 模拟为用户A提交任务
		manager.submitTask("userA", () -> System.out.println("User A Task 1"));
		manager.submitTask("userA", () -> System.out.println("User A Task 2"));
		manager.submitTask("userA", () -> System.out.println("User A Task 3"));

		// 模拟为用户B提交任务
		manager.submitTask("userB", () -> System.out.println("User B Task 1"));
		manager.submitTask("userB", () -> System.out.println("User B Task 2"));

		// 让程序继续运行一段时间
		Thread.sleep(3000);

		// 停止用户A的 Disruptor
		manager.stopUserDisruptor("userA");
	}

	// 获取或为用户创建新的 Disruptor 实例
	private Disruptor<UserTaskEvent> getUserDisruptor(String userId) {
		return userDisruptorMap.computeIfAbsent(userId, id -> {
			UserTaskEventFactory factory = new UserTaskEventFactory();
			UserTaskEventHandler handler = new UserTaskEventHandler();

			// 为每个用户创建独立的 Disruptor 实例
			Disruptor<UserTaskEvent> disruptor = new Disruptor<>(factory, 1024, executor, ProducerType.SINGLE, new BlockingWaitStrategy());
			//Group的消费者对于同一条消息m不重复消费
			// disruptor.handleEventsWithWorkerPool(consumers);
			//关键点 Group中的每个消费者都会对m进行消费，各个消费者之间不存在竞争
			disruptor.handleEventsWith(handler);
			disruptor.start();
			return disruptor;
		});
	}

	// 提交任务到用户的 Disruptor 实例
	public void submitTask(String userId, Runnable task) {
		Disruptor<UserTaskEvent> disruptor = getUserDisruptor(userId);
		disruptor.publishEvent((event, sequence) -> event.setTask(task));
	}

	// 停止某个用户的 Disruptor 实例
	public void stopUserDisruptor(String userId) {
		Disruptor<UserTaskEvent> disruptor = userDisruptorMap.remove(userId);
		if (disruptor != null) {
			disruptor.shutdown();
		}
	}
}
