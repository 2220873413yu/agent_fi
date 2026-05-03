package com.xms.common.config.redis.ratelimiter;

import com.xms.common.annotation.RateLimiter;
import com.xms.common.config.redis.lock.RedisLockAspect;
import com.xms.common.enums.LimitType;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.ServletUtils;
import com.xms.common.utils.StringUtil;
import com.xms.common.utils.StringUtils;
import com.xms.common.utils.ip.IpUtils;
import com.xms.common.utils.spel.RenegadeExpressionEvaluator;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 限流处理
 *
 * @author MIER
 */
@Aspect
@Component
public class RateLimiterAspect implements ApplicationContextAware {
	private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);
	/**
	 * 表达式处理
	 */
	private static final RenegadeExpressionEvaluator EVALUATOR = new RenegadeExpressionEvaluator();
	private RedisTemplate<Object, Object> redisTemplate;
	private ApplicationContext applicationContext;
	private RedisScript<Long> limitScript;

	@Autowired
	public void setRedisTemplate1(RedisTemplate<Object, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Autowired
	public void setLimitScript(RedisScript<Long> limitScript) {
		this.limitScript = limitScript;
	}

	@Before("@annotation(rateLimiter)")
	public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable {
		int time = rateLimiter.time();
		int count = rateLimiter.count();

		String combineKey = getCombineKey(rateLimiter, point);
		List<Object> keys = Collections.singletonList(combineKey);
		try {
			Long number = redisTemplate.execute(limitScript, keys, count, time);
			if (StringUtils.isNull(number) || number.intValue() > count) {
				throw new ServiceException("访问过于频繁，请稍候再试");
			}
			log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), combineKey);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("服务器限流异常，请稍候再试");
		}
	}

	/**
	 * @param point
	 * @return
	 */
	public String getCombineKey(RateLimiter rateLimiter, JoinPoint point) {
		StringBuilder stringBuffer = new StringBuilder(rateLimiter.key());
		if (rateLimiter.limitType() == LimitType.IP) {
			stringBuffer.append(IpUtils.getIpAddr(ServletUtils.getRequest())).append("-");
		}
		// el 表达式
		String limitParam = rateLimiter.param();
		if (StringUtil.isNotBlank(limitParam)) {
			String evalAsText = RedisLockAspect.evalLockParam((ProceedingJoinPoint) point, limitParam, EVALUATOR, applicationContext);
			stringBuffer.append(evalAsText).append("-");
		}
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		Class<?> targetClass = method.getDeclaringClass();
		stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
		return stringBuffer.toString();
	}


	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
