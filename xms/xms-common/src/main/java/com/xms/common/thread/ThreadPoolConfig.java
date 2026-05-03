package com.xms.common.thread;

import com.xms.common.utils.ThreadLocalUtil;
import com.xms.common.utils.Threads;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ExecutorConfigurationSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程池配置
 **/
@Configuration
public class ThreadPoolConfig {
	// 核心线程池大小
	private int corePoolSize = 50;

	// 最大可创建的线程数
	private int maxPoolSize = 200;

	// 队列最大长度
	private int queueCapacity = 1000;

	// 线程池维护线程所允许的空闲时间
	private int keepAliveSeconds = 300;

	@Bean(name = "threadPoolTaskExecutor")
	public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setMaxPoolSize(maxPoolSize);
		executor.setCorePoolSize(corePoolSize);
		executor.setQueueCapacity(queueCapacity);
		executor.setKeepAliveSeconds(keepAliveSeconds);
		executor.setTaskDecorator(XmsRunnableWrapper::new);
		// 线程池对拒绝任务(无线程可用)的处理策略
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return executor;
	}


	/**
	 * 虚拟线程执行器
	 *
	 * @return
	 */
	@Bean(name = "asyncVirtualExecutor")
	public Executor asyncVirtualExecutor() {
		ExecutorService virtualExecutor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("virtual-async-GT63S-", 1).factory());
		return runnable -> virtualExecutor.execute(new ContextCopyingDecorator().decorate(runnable));

	}

	/**
	 * 业务线程池
	 *
	 * @return
	 */
	@Bean(name = "asyncExecutor")
	public Executor asyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		// 核心线程数（默认线程数）线程池创建时候初始化的线程数
		executor.setCorePoolSize(19);
		// 缓冲队列数 用来缓冲执行任务的队列
		executor.setQueueCapacity(281);
		// 最大线程数 线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
		executor.setMaxPoolSize(9999);
		// 允许线程空闲时间（单位：默认为秒）当超过了核心线程之外的线程在空闲时间到达之后会被销毁
		executor.setKeepAliveSeconds(300);
		//用来设置线程池关闭的时候等待所有任务都完成再继续销毁其他的Bean
		executor.setWaitForTasksToCompleteOnShutdown(true);
		//该方法用来设置线程池中任务的等待时间，如果超过这个时候还没有销毁就强制销毁，以确保应用最后能够被关闭，而不是阻塞住。
		executor.setAwaitTerminationSeconds(120);
		executor.setThreadNamePrefix("asyncTread-四叶草-AMG-2.9T-");
		//设置拒绝策略，当任务源源不断的过来，而我们的系统又处理不过来的时候，我们要采取的策略是拒绝服务。
		/**
		 * 四种处理策略。
		 * CallerRunsPolicy：这个策略显然不想放弃执行任务。但是由于池中已经没有任何资源了，那么就直接使用调用该execute的线程本身来执行。（开始我总不想丢弃任务的执行，但是对某些应用场景来讲，很有可能造成当前线程也被阻塞。如果所有线程都是不能执行的，很可能导致程序没法继续跑了。需要视业务情景而定吧。）
		 * AbortPolicy这种策略直接抛出异常，丢弃任务。（jdk默认策略，队列满并线程满时直接拒绝添加新任务，并抛出异常，所以说有时候放弃也是一种勇气，为了保证后续任务的正常进行，丢弃一些也是可以接收的，记得做好记录）
		 *DiscardPolicy 这种策略和AbortPolicy几乎一样，也是丢弃任务，只不过他不抛出异常。
		 *DiscardOldestPolicy 如果执行程序尚未关闭，则位于工作队列头部的任务将被删除，然后重试执行程序（如果再次失败，则重复此过程）
		 */
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		//设置父子线程上下文的干活
		executor.setTaskDecorator(XmsRunnableWrapper::new);
		//执行初始化
		executor.initialize();
		return executor;
	}

	/**
	 * 执行周期性或定时任务
	 */
	@Bean(name = "scheduledExecutorService")
	protected ScheduledExecutorService scheduledExecutorService() {
		return new ScheduledThreadPoolExecutor(corePoolSize,
			new BasicThreadFactory.Builder().namingPattern("schedule-pool-%d").daemon(true).build(),
			new ThreadPoolExecutor.CallerRunsPolicy()) {
			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				super.afterExecute(r, t);
				Threads.printException(r, t);
			}
		};
	}

	// 自定义 TaskDecorator，用于在虚拟线程中传递上下文信息
	private static class ContextCopyingDecorator implements TaskDecorator {

		@Override
		public Runnable decorate(Runnable runnable) {
			// 获取当前上下文（如 traceId）
			Map<String, String> mdcMap = MDC.getCopyOfContextMap();
			return () -> {
				try {
					if (mdcMap != null && !mdcMap.isEmpty()) {
						MDC.setContextMap(mdcMap);
					}
					runnable.run();
				} finally {
					// 清理上下文
					if (mdcMap != null) {
						mdcMap.clear();
					}
					MDC.clear();
				}
			};
		}
	}
}
