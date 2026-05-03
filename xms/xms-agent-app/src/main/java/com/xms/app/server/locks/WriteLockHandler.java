package com.xms.app.server.locks;

/**
 * @author: renengadePISTA
 * @createDate: 2023/10/18
 */
public interface WriteLockHandler<T> {
	/**
	 * @param t
	 */
	void handlerWrite(T t);

}
