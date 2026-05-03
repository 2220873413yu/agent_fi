package com.xms.common.annotation.impl;

import cn.hutool.core.util.IdUtil;
import com.xms.common.constant.SysConstant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;

import java.util.Map;

/**
 * 为异步方法添加correlationId
 *
 * @author MIER
 */
@Aspect
public class LogTraceAspect {

	@Pointcut("@annotation(org.springframework.scheduling.annotation.Async)")
	public void logPointCut() {
	}

	@Around("logPointCut()")
	public Object around(ProceedingJoinPoint point) throws Throwable {
		Map<String, String> mdcMap = MDC.getCopyOfContextMap();
		if (mdcMap != null && !mdcMap.isEmpty()) {
			MDC.setContextMap(mdcMap);
		} else {
			MDC.put(SysConstant.CORRELATION_ID, IdUtil.fastUUID());
		}
		Object result = point.proceed();
		if (mdcMap != null) {
			mdcMap.clear();
		}
		MDC.clear();
		return result;
	}
}
