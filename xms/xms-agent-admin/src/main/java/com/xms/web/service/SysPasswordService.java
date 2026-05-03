package com.xms.web.service;

import java.util.concurrent.TimeUnit;

import com.xms.common.config.redis.XmsRedis;
import com.xms.common.thread.AsyncManager;
import com.xms.web.manager.factory.AsyncFactory;
import com.xms.web.security.context.AuthenticationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.xms.common.constant.CacheConstants;
import com.xms.common.constant.Constants;
import com.xms.common.core.domain.entity.SysUser;
import com.xms.common.exception.user.UserPasswordNotMatchException;
import com.xms.common.exception.user.UserPasswordRetryLimitExceedException;
import com.xms.common.utils.MessageUtils;
import com.xms.common.utils.SecurityUtils;

/**
 * 登录密码方法
 *
 *
 */
@Component
public class SysPasswordService
{
    @Autowired
    private XmsRedis redisCache;

    @Value(value = "${user.password.maxRetryCount}")
    private int maxRetryCount;

    @Value(value = "${user.password.lockTime}")
    private int lockTime;

    /**
     * 登录账户密码错误次数缓存键名
     *
     * @param username 用户名
     * @return 缓存键key
     */
    private String getCacheKey(String username)
    {
        return CacheConstants.PWD_ERR_CNT_KEY + username;
    }

    public void validate(SysUser user)
    {
        Authentication usernamePasswordAuthenticationToken = AuthenticationContextHolder.getContext();
        String username = usernamePasswordAuthenticationToken.getName();
        String password = usernamePasswordAuthenticationToken.getCredentials().toString();

        Integer retryCount = redisCache.get(getCacheKey(username));

        if (retryCount == null)
        {
            retryCount = 0;
        }

        if (retryCount >= Integer.valueOf(maxRetryCount).intValue())
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.retry.limit.exceed", maxRetryCount, lockTime)));
            throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
        }

        if (!matches(user, password))
        {
            retryCount = retryCount + 1;
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL,
                    MessageUtils.message("user.password.retry.limit.count", retryCount)));
            redisCache.set(getCacheKey(username), retryCount, (long)lockTime, TimeUnit.MINUTES);
            throw new UserPasswordNotMatchException();
        }
        else
        {
            clearLoginRecordCache(username);
        }
    }

    public boolean matches(SysUser user, String rawPassword)
    {
        return SecurityUtils.matchesPassword(rawPassword, user.getPassword());
    }

    public void clearLoginRecordCache(String loginName)
    {
        if (redisCache.hasKey(getCacheKey(loginName)))
        {
            redisCache.del(getCacheKey(loginName));
        }
    }
}
