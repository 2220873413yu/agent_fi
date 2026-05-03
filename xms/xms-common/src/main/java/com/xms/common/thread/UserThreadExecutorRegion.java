package com.xms.common.thread;


/**
 * 用户线程执行器管理域
 * <pre>
 *     执行器具体数量是不大于 Runtime.getRuntime().availableProcessors() 的 2 次幂。
 *     当 availableProcessors 的值分别为 4、8、12、16、32 时，对应的数量则是 4、8、8、16、32。
 *
 *     4、8、12、16、32 （availableProcessors 的值）
 *     4、8、 8、16、32 （对应的数量）
 * </pre>
 * <pre>
 *     UserThreadExecutorRegion - 用户线程执行器管理域
 *     该执行器主要用于消费 action 业务，或者说消费玩家相关的业务。
 *     通过 userId 来得到对应的 ThreadExecutor 执行业务，从而避免并发问题。
 * </pre>
 */
final class UserThreadExecutorRegion extends AbstractThreadExecutorRegion {
	final int executorLength;

	UserThreadExecutorRegion(String threadName) {
		//多减1目的，数组从0开始的，防止越级
		super("threadName", RuntimeKit.availableLatestProcessors2n);
		this.executorLength = RuntimeKit.availableLatestProcessors2n - 1;
	}

	public static void main(String[] args) {
		int res = RuntimeKit.availableProcessors * 2 - 2;
		System.out.println(res);
		System.out.println(1001 & res);
	}

	/**
	 * 根据 userId 获取对应的 Executor
	 *
	 * @param userId userId
	 * @return Executor 任务执行器
	 */
	@Override
	public ThreadExecutor getThreadExecutor(long userId) {
		//只有2的幂 才能位运算，注意
		int index = (int) (userId & this.executorLength);
		return this.threadExecutors[index];
	}
}
