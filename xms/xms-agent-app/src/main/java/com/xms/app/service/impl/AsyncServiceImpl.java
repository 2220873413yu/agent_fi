package com.xms.app.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.fastjson2.JSONObject;
import com.xms.app.service.AsyncService;
import com.xms.common.config.redis.stream.XmsRedisStreamListener;
import com.xms.common.utils.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.Record;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.validation.ObjectError;

import java.util.Map;

/**
 * PISTA
 */
@Slf4j
@Service
public class AsyncServiceImpl implements AsyncService {

	/**
	 * 在需要重试的方法上添加 @Retryable 注解
	 * 其里面属性说明
	 * // 设置重试拦截器的 bean 名称
	 * String interceptor() default "";
	 * <p>
	 * // 只对特定类型的异常进行重试。默认：所有异常
	 * Class<? extends Throwable>[] value() default {};
	 * <p>
	 * // 包含或者排除哪些异常进行重试
	 * Class<? extends Throwable>[] include() default {};
	 * Class<? extends Throwable>[] exclude() default {};
	 * <p>
	 * // l设置该重试的唯一标志，用于统计输出
	 * String label() default "";
	 * <p>
	 * boolean stateful() default false;
	 * <p>
	 * // 最大重试次数，默认为 3 次
	 * int maxAttempts() default 3;
	 * <p>
	 * String maxAttemptsExpression() default "";
	 * <p>
	 * // 设置重试补偿机制，可以设置重试间隔，并且支持设置重试延迟倍数
	 * Backoff backoff() default @Backoff;
	 * <p>
	 * // 异常表达式，在抛出异常后执行，以判断后续是否进行重试
	 * String exceptionExpression() default "";
	 * <p>
	 * String[] listeners() default {};
	 * maxAttempts 最大重试次数 默认3
	 * -- @Backoff 重试策略   delay 重试间隔时间，单位毫秒， multiplier 重试间隔倍数
	 * 最大重试次数为 3，第一次重试间隔为 2s，之后以 2 倍大小进行递增，第二次重试间隔为 4 s，第三次为 8s
	 * 注意：避免类class调用本类方法
	 */
	@Async("asyncExecutor")
	@Override
	@Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000, multiplier = 2))
	public void handerDemoContext() {
		System.out.println("异步开始的干活");
		log.info("上线文获取的东西：{}", ThreadLocalUtil.getAll());
		System.out.println("重试框架测试");
		int i = 3 / 0;
	}

	@Async("asyncVirtualExecutor")
	@Override
	public void testVirtualDeamo() {
		log.info("virtual 执行demo：{}", Thread.currentThread().getName());
	}

	/**
	 * -- @Recover 注解： 进行善后工作：当重试达到指定次数之后，会调用指定的方法来进行日志记录等操作
	 * 注意：
	 * - -@Recover 注解标记的方法必须和被 @Retryable 标记的方法在同一个类中
	 * <p>
	 * 重试方法抛出的异常类型需要与 recover() 方法参数类型保持一致
	 * <p>
	 * recover() 方法返回值需要与重试方法返回值保证一致
	 * <p>
	 * recover() 方法中不能再抛出 Exception，否则会报无法识别该异常的错误
	 */
	@Recover
	public void recover(Exception e) {
		log.error("达到最大重试次数", e);
	}


	// @XmsRedisStreamListener(name = "testXmsJMS", readRawBytes = true)
	public void testXmsJMS(Record<String, Map<String, byte[]>> record) {

		System.out.println("testXmsJMS 消息测速");
		String str = new String(record.getValue().get("DisruptorMsg"));
		log.info("DisruptorMsg: {}", str);
		System.out.println(record);
	}
}
