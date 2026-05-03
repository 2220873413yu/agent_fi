package com.xms.common.thread;

/**
 * 线程执行器管理域
 * <pre>
 *     支持指定执行器来消费任务
 * </pre>
 *
 */
public interface ThreadExecutorRegion {
    /**
     * 根据 index 获取对应的 Executor
     *
     * @param index index 不能是负数
     * @return Executor 线程执行器
     */
    ThreadExecutor getThreadExecutor(long index);

    /**
     * 根据 index 获取对应的 Executor 来执行任务
     *
     * @param runnable 任务
     * @param index    index
     */
    default void execute(Runnable runnable, long index) {
        this.getThreadExecutor(index).execute(runnable);
    }
}
