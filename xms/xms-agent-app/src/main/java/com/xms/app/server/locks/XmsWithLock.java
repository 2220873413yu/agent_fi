package com.xms.app.server.locks;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * 手撸读写锁
 *
 * @author: renengadePISTA
 * @createDate: 2023/10/18
 */
@Slf4j
public class XmsWithLock<T> {
	private static final long serialVersionUID = -3048283373239453901L;


	/**
	 *
	 */
	private T obj;

	/**
	 *
	 */
	private ReentrantReadWriteLock lock = null;

	/**
	 * @param obj
	 */
	public XmsWithLock(T obj) {
		this(obj, new ReentrantReadWriteLock());
	}

	/**
	 * @param obj
	 * @param lock
	 * @author tanyaowu
	 */
	public XmsWithLock(T obj, ReentrantReadWriteLock lock) {
		super();
		this.obj = obj;
		this.lock = lock;
	}

	/**
	 *
	 */
	public ReentrantReadWriteLock getLock() {
		return lock;
	}

	/**
	 * 获取写锁
	 */
	public WriteLock writeLock() {
		return lock.writeLock();
	}

	/**
	 * 获取读锁
	 */
	public ReadLock readLock() {
		return lock.readLock();
	}

	/**
	 *
	 */
	public T getObj() {
		return obj;
	}

	/**
	 * @param obj
	 */
	public void setObj(T obj) {
		this.obj = obj;
	}

	/**
	 * 操作obj时，带上读锁，注意，不支持锁升级
	 *
	 * @param readLockHandler
	 */
	public void handlerRead(ReadLockHandler<T> readLockHandler) {
		ReadLock readLock = lock.readLock();
		readLock.lock();
		try {
			readLockHandler.handlerRead(obj);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * 操作obj时，带上写锁。支持锁降级
	 *
	 * @param writeLockHandler
	 */
	public void handlerWrite(WriteLockHandler<T> writeLockHandler) {
		WriteLock writeLock = lock.writeLock();
		writeLock.lock();
		try {
			writeLockHandler.handlerWrite(obj);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
	}
}
