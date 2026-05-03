package com.xms.common.thread.asyncTool;

import com.jd.platform.async.executor.Async;
import com.xms.common.thread.ExecutorRegion;
import com.xms.common.thread.ExecutorRegionKit;
import com.xms.common.thread.ThreadExecutor;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 增强版，支持虚拟线程
 *
 * @author: GT63S
 * @createDate: 2024/9/2
 */
public class AsyncPlus extends Async {
	private static final ExecutorRegion COMMON_POOL = ExecutorRegionKit.getExecutorRegion();

	/**
	 * 出发点
	 */
	public static boolean beginWork(long timeout, ExecutorRegion executorRegion, List<WorkerWrapper> workerWrappers) throws ExecutionException, InterruptedException {
		if (workerWrappers == null || workerWrappers.isEmpty()) {
			return false;
		}
		//定义一个map，存放所有的wrapper，key为wrapper的唯一id，value是该wrapper，可以从value中获取wrapper的result
		Map<String, WorkerWrapper> forParamUseWrappers = new ConcurrentHashMap<>();
		CompletableFuture[] futures = new CompletableFuture[workerWrappers.size()];
		for (int i = 0; i < workerWrappers.size(); i++) {
			WorkerWrapper wrapper = workerWrappers.get(i);
			ThreadExecutor userVirtualThreadExecutor = executorRegion.getBlockUserVirtualThreadExecutor(Long.parseLong(wrapper.getId()),false);
			Executor executorService = userVirtualThreadExecutor.getRealExecutor();
			futures[i] = CompletableFuture.runAsync(() -> wrapper.work(executorRegion, timeout, forParamUseWrappers), executorService);
		}
		try {
			CompletableFuture.allOf(futures).get(timeout, TimeUnit.MILLISECONDS);
			return true;
		} catch (TimeoutException e) {
			Set<WorkerWrapper> set = new HashSet<>();
			totalWorkers(workerWrappers, set);
			for (WorkerWrapper wrapper : set) {
				wrapper.stopNow();
			}
			return false;
		}
	}


	/**
	 * 如果想自定义线程池，请传pool。不自定义的话，就走默认的COMMON_POOL
	 */
	public static boolean beginWork(long timeout, ExecutorRegion executorService, WorkerWrapper... workerWrapper) throws ExecutionException, InterruptedException {
		if (workerWrapper == null || workerWrapper.length == 0) {
			return false;
		}
		List<WorkerWrapper> workerWrappers = Arrays.stream(workerWrapper).collect(Collectors.toList());
		return beginWork(timeout, executorService, workerWrappers);
	}

	/**
	 * 同步阻塞,直到所有都完成,或失败
	 */
	public static boolean beginWork(long timeout, WorkerWrapper... workerWrapper) throws ExecutionException, InterruptedException {
		return beginWork(timeout, COMMON_POOL, workerWrapper);
	}


	/**
	 * 总共多少个执行单元
	 */
	@SuppressWarnings("unchecked")
	private static void totalWorkers(List<WorkerWrapper> workerWrappers, Set<WorkerWrapper> set) {
		set.addAll(workerWrappers);
		for (WorkerWrapper wrapper : workerWrappers) {
			if (wrapper.getNextWrappers() == null) {
				continue;
			}
			List<WorkerWrapper> wrappers = wrapper.getNextWrappers();
			totalWorkers(wrappers, set);
		}

	}


}
