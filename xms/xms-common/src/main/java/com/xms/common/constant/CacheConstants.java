package com.xms.common.constant;

/**
 * 缓存的key 常量
 *
 *
 */
public class CacheConstants
{
    /**
     * 登录用户 redis key
     */
    public static final String LOGIN_TOKEN_KEY = RedisConstant.REDIS_PREFIX + "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY =  RedisConstant.REDIS_PREFIX +"captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = RedisConstant.REDIS_PREFIX + "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = RedisConstant.REDIS_PREFIX + "sys_dict:";

    /**
     * 防重提交 redis key
     */
    public static final String REPEAT_SUBMIT_KEY =  RedisConstant.REDIS_PREFIX +"repeat_submit:";

	/**
	 * 防短时间内登录 redis key
	 */
	public static final String REPEAT_SUBMIT_LOGIN_KEY =  RedisConstant.REDIS_PREFIX +"repeat_login_submit:";

    /**
     * 限流 redis key
     */
    public static final String RATE_LIMIT_KEY =  RedisConstant.REDIS_PREFIX +"rate_limit:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = RedisConstant.REDIS_PREFIX + "pwd_err_cnt:";

    /**
     * app登录用户 redis key
     */
    public static final String LOGIN_APP_TOKEN_KEY = RedisConstant.REDIS_PREFIX + "login_app_tokens:";
	/**
	 * APP登录账户密码错误次数 redis key
	 */
	public static final String APP_USER_PWD_ERR_CNT_KEY = RedisConstant.REDIS_PREFIX + "app_user_pwd_err_cnt:";
    /**
     * 用户googlekey
     */
    public static String user_google_auth_key = RedisConstant.REDIS_PREFIX + "user_google_auth_key";
}
