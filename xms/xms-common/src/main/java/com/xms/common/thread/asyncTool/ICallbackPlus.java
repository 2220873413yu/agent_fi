package com.xms.common.thread.asyncTool;

import com.jd.platform.async.callback.ICallback;
import com.jd.platform.async.worker.WorkResult;

/**
 * @author: renengadePISTA
 * @createDate: 2024/9/2 15:58
 */
@FunctionalInterface
public interface ICallbackPlus<T, V> extends ICallback<T, V> {
	@Override
	default void begin(){
	};

	@Override
	void result(boolean success, T param, WorkResult<V> workResult);
}
