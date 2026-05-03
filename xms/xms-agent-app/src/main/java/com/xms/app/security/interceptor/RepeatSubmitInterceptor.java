package com.xms.app.security.interceptor;

import com.alibaba.fastjson.JSON;
import com.xms.app.security.filter.JwtAuthenticationAppTokenFilter;
import com.xms.common.annotation.RateLimiter;
import com.xms.common.annotation.RepeatSubmit;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.lock.RedisLockAspect;
import com.xms.common.constant.CacheConstants;
import com.xms.common.enums.LimitType;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.result.ResponseWrap;
import com.xms.common.utils.ServletUtils;

import com.xms.common.utils.StringUtil;
import com.xms.common.utils.StringUtils;
import com.xms.common.utils.ip.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 防止重复提交拦截器
 *
 *
 */
@Component
@Slf4j
public abstract class RepeatSubmitInterceptor implements HandlerInterceptor
{
	@Autowired
	private XmsRedis redisCache;

	@Autowired
	private RedisTemplate<Object, Object> redisTemplate;

	@Autowired
	private RedisScript<Long> limitScript;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception
    {
        if (handler instanceof HandlerMethod)
        {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod();
            RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
			// 请求地址（作为存放cache的key值）
			//todo 此版本注释掉 不需要该功能
//			String url = request.getRequestURI();
//			if("/userinfo/login".equals(url)){
//				loginCheck();
//			}
            if (annotation != null)
            {
                if (this.isRepeatSubmit(request, annotation))
                {
                	ResponseWrap<String> responseWrap = ResponseWrap.dataError(ResponseCode.CODE_1001);
                    ServletUtils.renderString(response, JSON.toJSONString(responseWrap));
                    return false;
                }
            }
            return true;
        }
        else
        {
            return true;
        }
    }

	/**
	 * 登录错误次数达到阈值 就封禁ip
	 * @return
	 */
//	private boolean loginCheck() {
//		String resIp = IpUtils.getIpAddr(ServletUtils.getRequest());
//		String cacheLoginKey = CacheConstants.REPEAT_SUBMIT_LOGIN_KEY + resIp;
//		//加登录错误次数+封号
//		redisCache.incr(cacheLoginKey);
//		//密码错误次数达到阈值 就需要冻结账号
//		redisCache.expire(cacheLoginKey, 10L, TimeUnit.SECONDS);
//		//判断超过了就禁止登录
//		Integer retryCount = redisCache.get(cacheLoginKey);
//		if (retryCount == null)
//		{
//			retryCount = 0;
//		}
//
//		if (retryCount >= 3){
//			//加入到ip拦截
//			JwtAuthenticationAppTokenFilter.ipAddressList.add(resIp);
//			log.error("IP黑名单拦截,{}", resIp);
//			return true;
//		}
//		return false;
//	}

	private void loginCheck() {
		String resIp = IpUtils.getIpAddr(ServletUtils.getRequest());
		int time = 30;
		int count = 5;
		String combineKey = CacheConstants.REPEAT_SUBMIT_LOGIN_KEY + resIp;
		List<Object> keys = Collections.singletonList(combineKey);
		try {
			Long number = redisTemplate.execute(limitScript, keys, count, time);
			if (StringUtils.isNull(number) || number.intValue() > count) {
			JwtAuthenticationAppTokenFilter.ipAddressList.add(resIp);
				log.error("IP黑名单拦截,{}", resIp);
				throw new ServiceException(ResponseCode.CODE_107);
			}
			log.info("限制请求'{}',当前请求'{}',缓存key'{}'", count, number.intValue(), combineKey);
		} catch (ServiceException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("服务器限流异常，请稍候再试");
		}
	}

//	public String getCombineKey(String resIp) {
//		StringBuilder stringBuffer = new StringBuilder(rateLimiter.key());
//		if (rateLimiter.limitType() == LimitType.IP) {
//			stringBuffer.append(IpUtils.getIpAddr(ServletUtils.getRequest())).append("-");
//		}
//		// el 表达式
//		String limitParam = rateLimiter.param();
//		if (StringUtil.isNotBlank(limitParam)) {
//			String evalAsText = RedisLockAspect.evalLockParam((ProceedingJoinPoint) point, limitParam, EVALUATOR, applicationContext);
//			stringBuffer.append(evalAsText).append("-");
//		}
//		MethodSignature signature = (MethodSignature) point.getSignature();
//		Method method = signature.getMethod();
//		Class<?> targetClass = method.getDeclaringClass();
//		stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
//		return stringBuffer.toString();
//	}

	/**
     * 验证是否重复提交由子类实现具体的防重复提交的规则
     *
     * @param request
     * @return
     * @throws Exception
     */
    public abstract boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation);
}
