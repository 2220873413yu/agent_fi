package com.xms.common.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程执行器信息
 *
 * @param name     线程执行器名
 * @param executor 线程执行器
 * @param threadNo 编号
 */
@Slf4j
public record ThreadExecutor(String name, Executor executor, long threadNo) {
    /**
     * 在将来的某个时间执行给定的命令。
     * <pre>
     *     注意，此方法需要开发者自行捕获 command 中的异常（换句话说就是，不要让 Runnable 在执行时拋出异常），
     *     否则可能会引起 executor thread 损坏，从而使线程不可用。
     *
     *     如果无法确定 Runnable 在执行时是否会拋出异常，可以考虑使用 executeTry 方法。
     * </pre>
     *
     * @param command 命令
     * @see ThreadExecutor#executeTry(Runnable)
     */
    public void execute(Runnable command) {
        this.executor.execute(command);
    }


	public Executor getRealExecutor() {
		return  this.executor;
	}
    /**
     * 在将来的某个时间执行给定的命令。
     *
     * @param command 命令
     */
    public void executeTry(Runnable command) {
        this.executor.execute(() -> {
            try {
                command.run();
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public int getWorkQueue() {
        if (this.executor instanceof ThreadPoolExecutor threadPoolExecutor) {
            return threadPoolExecutor.getQueue().size();
        }

        return 0;
    }
}
