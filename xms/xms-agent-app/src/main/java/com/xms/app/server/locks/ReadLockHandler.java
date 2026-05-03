package com.xms.app.server.locks;

/**
 * @author: renengadePISTA
 * @createDate: 2023/10/18
 */
public interface ReadLockHandler<T> {
	/**
	 *
	 * @param t
	 */
	public void handlerRead(T t);

}
