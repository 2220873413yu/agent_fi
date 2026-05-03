/*
 *      Copyright (c) 2018-2028, DreamLu All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the dreamlu.net developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: DreamLu 卢春梦 (596392912@qq.com)
 */

package com.xms.common.config.redis.lock;

import com.xms.common.utils.CharPool;
import com.xms.common.utils.StringUtil;
import com.xms.common.utils.spel.RenegadeExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.Assert;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * redis 分布式锁
 *
 *
 */
@Aspect
@RequiredArgsConstructor
@Order(value = Ordered.HIGHEST_PRECEDENCE + 2)
public class RedisLockAspect implements ApplicationContextAware {

	/**
	 * 表达式处理
	 */
	private static final RenegadeExpressionEvaluator EVALUATOR = new RenegadeExpressionEvaluator();
	/**
	 * redis 限流服务
	 */
	private final RedisLockClient redisLockClient;
	private ApplicationContext applicationContext;

	/**
	 * 计算参数表达式
	 *   map 取值 #root['age']
	 *   bean 取值 #root.id
	 *   字段取值 #id
	 * @param point     ProceedingJoinPoint
	 * @param lockParam lockParam
	 * @return 结果
	 */
	public static String evalLockParam(ProceedingJoinPoint point, String lockParam, RenegadeExpressionEvaluator evaluator, ApplicationContext applicationContext) {
		MethodSignature ms = (MethodSignature) point.getSignature();
		Method method = ms.getMethod();
		Object[] args = point.getArgs();
		Object target = point.getTarget();
		Class<?> targetClass = target.getClass();
		EvaluationContext context = evaluator.createContext(method, args, target, targetClass, applicationContext);
		AnnotatedElementKey elementKey = new AnnotatedElementKey(method, targetClass);
		return evaluator.evalAsText(lockParam, elementKey, context);
	}

	/**
	 * AOP 环切 注解 @RedisLock
	 */
	@Around("@annotation(redisLock)")
	public Object aroundRedisLock(ProceedingJoinPoint point, RedisLock redisLock) {
		String lockName = redisLock.value();
		Assert.hasText(lockName, "@RedisLock value must have length; it must not be null or empty");
		// el 表达式
		String lockParam = redisLock.param();
		// 表达式不为空
		String lockKey;
		if (StringUtil.isNotBlank(lockParam)) {
			String evalAsText = evalLockParam(point, lockParam, EVALUATOR, applicationContext);
			lockKey = lockName + CharPool.COLON + evalAsText;
		} else {
			lockKey = lockName;
		}
		LockType lockType = redisLock.type();
		long waitTime = redisLock.waitTime();
		long leaseTime = redisLock.leaseTime();
		TimeUnit timeUnit = redisLock.timeUnit();
		return redisLockClient.lock(lockKey, lockType, waitTime, leaseTime, timeUnit, point::proceed);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
