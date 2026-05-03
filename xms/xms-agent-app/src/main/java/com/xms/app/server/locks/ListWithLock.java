package com.xms.app.server.locks;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * @author: renengadePISTA
 * @createDate: 2023/10/18
 */
@Slf4j
public class ListWithLock<T> extends XmsWithLock<List<T>> {
	private static final long serialVersionUID = 8549668315606224029L;

	public ListWithLock(List<T> list) {
		super(list);
	}

	/**
	 * @param lock
	 */
	public ListWithLock(List<T> list, ReentrantReadWriteLock lock) {
		super(list, lock);
	}

	/**
	 * @param t
	 * @return
	 */
	public boolean add(T t) {
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			List<T> list = this.getObj();
			return list.add(t);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
		return false;
	}


	public void clear() {
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			List<T> list = this.getObj();
			list.clear();
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * @param t
	 * @return
	 */
	public boolean remove(T t) {
		WriteLock writeLock = this.writeLock();
		writeLock.lock();
		try {
			List<T> list = this.getObj();
			return list.remove(t);
		} catch (Throwable e) {
			log.error(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
		return false;
	}

	/**
	 * @return
	 */
	public int size() {
		ReadLock readLock = this.readLock();
		readLock.lock();
		try {
			List<T> list = this.getObj();
			return list.size();
		} finally {
			readLock.unlock();
		}
	}
}
