package com.xms.common.thread;


import java.util.concurrent.ExecutorService;

/**
 * 用户虚拟线程执行器
 * <pre>
 *     该执行器主要用于消费 io 的相关业务（如 DB 入库）。
 * </pre>
 */
final class UserVirtualThreadExecutorRegion extends AbstractThreadExecutorRegion {
	final int executorLength;

	UserVirtualThreadExecutorRegion(String threadName, int size) {
		super(threadName, RuntimeKit.availableProcessors << size);
		this.executorLength = (RuntimeKit.availableProcessors << size) - 1;
	}

	static UserVirtualThreadExecutorRegion me() {
		return Holder.ME;
	}

	static UserVirtualThreadExecutorRegion blockMe() {
		return Holder.BLOCK_ME;
	}

	/**
	 * 根据 userId 获取对应的 Executor
	 *
	 * @param userId userId
	 * @return Executor 任务执行器
	 */
	@Override
	public ThreadExecutor getThreadExecutor(long userId) {
		if (userId >= this.executorLength) {
			int index = (int) (userId % this.executorLength);
			return this.threadExecutors[index];
		} else {
			return this.threadExecutors[(int) userId];
		}

	}

	@Override
	protected ExecutorService createExecutorService(String name) {
		return ExecutorKit.newVirtualExecutor(name);
	}

	/**
	 * 通过 JVM 的类加载机制, 保证只加载一次 (singleton)
	 */
	private static class Holder {
		static final UserVirtualThreadExecutorRegion ME = new UserVirtualThreadExecutorRegion("virtual-user-GT63S", 13);
		/**
		 * 阻塞耗时操作
		 */
		static final UserVirtualThreadExecutorRegion BLOCK_ME = new UserVirtualThreadExecutorRegion("virtual-user-block-GT63S", 14);
	}

}
