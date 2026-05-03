package com.xms.common.thread;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.*;

/**
 * 线程执行器管理域父类
 *
 */
@FieldDefaults(level = AccessLevel.PROTECTED)
abstract sealed class AbstractThreadExecutorRegion implements ThreadExecutorRegion
        permits
        UserThreadExecutorRegion,
        UserVirtualThreadExecutorRegion {

    /** 线程执行器 */
    final ThreadExecutor[] threadExecutors;

    AbstractThreadExecutorRegion(String threadName, int executorSize) {
        this.threadExecutors = new ThreadExecutor[executorSize];

        for (int i = 0; i < executorSize; i++) {
            // 线程名：name-线程总数-当前线程编号
            int threadNo = i + 1;
            String threadNamePrefix = String.format("%s-%s-%s", threadName, executorSize, threadNo);
            var executor = this.createExecutorService(threadNamePrefix);
            this.threadExecutors[i] = new ThreadExecutor(threadNamePrefix, executor, threadNo);
        }
    }

    protected ExecutorService createExecutorService(String name) {
        ThreadFactory threadFactory = new FixedNameThreadFactory(name);

        return new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory);
    }
}
