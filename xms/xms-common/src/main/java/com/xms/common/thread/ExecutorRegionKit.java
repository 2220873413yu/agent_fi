package com.xms.common.thread;

import com.xms.common.constant.SysConstant;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ExecutorRegion 工具类，起到类似代理的作用。
 *
 * @see UserThreadExecutorRegion 用户线程执行器管理域
 * @see UserVirtualThreadExecutorRegion 用户虚拟线程执行器
 * @see ExecutorRegion 线程执行器 region
 */
@UtilityClass
public class ExecutorRegionKit {
	/**
	 * 定制线程策略
	 */
	@Setter
	private final Function<String, ExecutorRegion> executorRegionSupplierByName = (threadName) -> new ExecutorRegion() {
		final UserThreadExecutorRegion userThreadExecutorRegion = new UserThreadExecutorRegion(threadName);

		@Override
		public ThreadExecutorRegion getUserThreadExecutorRegion() {
			return this.userThreadExecutorRegion;
		}
	};
	private final ConcurrentHashMap<String, ExecutorRegion> regionThreadCache = new ConcurrentHashMap<>(SysConstant.EIGHT);
	/**
	 * 定制线程策略
	 */
	@Setter
	Supplier<ExecutorRegion> executorRegionSupplier = () -> new ExecutorRegion() {
		final UserThreadExecutorRegion userThreadExecutorRegion = new UserThreadExecutorRegion("user-GT63S");

		@Override
		public ThreadExecutorRegion getUserThreadExecutorRegion() {
			return this.userThreadExecutorRegion;
		}
	};
	/**
	 * 初始化时就注入属性了，只要b不set，肯定都是单例
	 */
	@Setter
	@Getter
	ExecutorRegion executorRegion = executorRegionSupplier.get();

	/**
	 * 每次调用返回不同的对象，保证不同业务可以有不同的thread
	 *
	 * @return
	 */
	public ExecutorRegion createExecutorRegion() {
		return executorRegionSupplier.get();
	}

	/**
	 * 每次调用返回不同的对象，保证不同业务可以有不同的thread
	 *
	 * @return
	 */
	public ExecutorRegion createExecutorRegion(String threadName) {
		return regionThreadCache.computeIfAbsent(threadName, executorRegionSupplierByName);
	}


}
