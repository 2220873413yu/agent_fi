package com.xms.common.thread;

/**
 * 执行器管理域，管理着 {@link ThreadExecutorRegion} （线程执行器管理域）的实现类
 * <pre>
 *     {@link UserThreadExecutorRegion} - 用户线程执行器管理域
 *     该执行器主要用于消费 action 业务，或者说消费玩家相关的业务。
 *     通过 userId 来得到对应的 ThreadExecutor 执行业务，从而避免并发问题。
 *
 *     {@link UserVirtualThreadExecutorRegion} - 用户虚拟线程执行器
 *     该执行器主要用于消费 io 的相关业务（如 DB 入库）。
 *
 *     可通过 index 来得到对应的 ThreadExecutor 执行业务，从而避免并发问题。
 *     如果业务是计算密集型的，又不想占用 {@link UserThreadExecutorRegion} 线程资源时，可使用该执行器。
 * </pre>
 *
 * @see UserThreadExecutorRegion 用户线程执行器管理域
 * @see UserVirtualThreadExecutorRegion 用户虚拟线程执行器
 */
public interface ExecutorRegion {
	/**
	 * user 线程执行器管理域
	 *
	 * @return user 线程执行器管理域
	 */
	ThreadExecutorRegion getUserThreadExecutorRegion();

	/**
	 * 用户虚拟线程执行器
	 *
	 * @return 用户虚拟线程执行器
	 */
	default ThreadExecutorRegion getUserVirtualThreadExecutorRegion() {
		return UserVirtualThreadExecutorRegion.me();
	}

	/**
	 * 用户虚拟线程执行器阻塞耗时的
	 *
	 * @return 用户虚拟线程执行器
	 */
	default ThreadExecutorRegion getBlockUserVirtualThreadExecutorRegion() {
		return UserVirtualThreadExecutorRegion.blockMe();
	}

	/**
	 * user 线程执行器管理域,保证有序操作
	 *
	 * @param index index
	 * @return user 线程执行器管理域
	 */
	default ThreadExecutor getUserThreadExecutor(long index) {
		return this.getUserThreadExecutorRegion().getThreadExecutor(index);
	}

	/**
	 * 用户虚拟线程执行器，目前版本由于jdk bug，无法保证有序操作哟
	 *
	 * @param index index
	 * @return 用户虚拟线程执行器
	 */
	default ThreadExecutor getUserVirtualThreadExecutor(long index) {
		return this.getUserVirtualThreadExecutorRegion().getThreadExecutor(index);
	}

	/**
	 * 用户虚拟线程执行器
	 *
	 * @param index index
	 * @param flag  flag 是否从预热过的虚拟线程取，不需要那么就直接创建
	 * @return 用户虚拟线程执行器
	 */
	default ThreadExecutor getBlockUserVirtualThreadExecutor(long index, boolean flag) {
		if (flag) {
			return this.getBlockUserVirtualThreadExecutorRegion().getThreadExecutor(index);
		} else {
			String threadNamePrefix = String.format("%s-%s", "virtual-block-GT63S", index);
			var executor = ExecutorKit.newVirtualExecutor(threadNamePrefix);
			return new ThreadExecutor(threadNamePrefix, executor, index);
		}
	}
}
