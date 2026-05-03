package com.xms.common.thread.asyncTool;


import com.jd.platform.async.callback.IWorker;

import java.util.Map;

/**
 * @author: GT63S
 * @createDate: 2024/9/2
 */
@FunctionalInterface
public interface IWorkerPlus<T, V> extends IWorker<T, V> {
	/**
	 * 在这里做耗时操作，如rpc请求、IO等
	 *
	 * @param object object
	 */
	@Override
	default V action(T object, Map<String, com.jd.platform.async.wrapper.WorkerWrapper> allWrappers) {
		return null;
	}

	/**
	 * 超时、异常时，返回的默认值
	 *
	 * @return 默认值
	 */
	@Override
	default V defaultValue() {
		return null;
	}

	/**
	 * 在这里做耗时操作，如rpc请求、IO等
	 *
	 * @param object object
	 */
	V actionVirtual(T object, Map<String, WorkerWrapper> allWrappers);

}
