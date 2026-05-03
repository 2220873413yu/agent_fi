package com.xms.common.constant;

import io.jsonwebtoken.Claims;

/**
 * 通用常量信息
 */
public class Constants {

	/**
	 * 英文
	 */
	public static final String EN="en";

	/**
	 * 简体中文
	 */
	public static final String ZH="zh";

	/**
	 * 繁体中文
	 */
	public static final String TW="tw";

	/**
	 * UTF-8 字符集
	 */
	public static final String UTF8 = "UTF-8";

	/**
	 * GBK 字符集
	 */
	public static final String GBK = "GBK";

	/**
	 * www主域
	 */
	public static final String WWW = "www.";

	/**
	 * http请求
	 */
	public static final String HTTP = "http://";

	/**
	 * https请求
	 */
	public static final String HTTPS = "https://";

	/**
	 * 通用成功标识
	 */
	public static final String SUCCESS = "0";

	/**
	 * 通用失败标识
	 */
	public static final String FAIL = "1";

	/**
	 * 登录成功
	 */
	public static final String LOGIN_SUCCESS = "Success";

	/**
	 * 注销
	 */
	public static final String LOGOUT = "Logout";

	/**
	 * 注册
	 */
	public static final String REGISTER = "Register";

	/**
	 * 登录失败
	 */
	public static final String LOGIN_FAIL = "Error";

	/**
	 * 验证码有效期（分钟）
	 */
	public static final Integer CAPTCHA_EXPIRATION = 2;

	/**
	 * 令牌
	 */
	public static final String TOKEN = "token";

	/**
	 * 邮箱验证code
	 */
	public static final String CODE = "code";

	/**
	 * 令牌前缀
	 */
	public static final String TOKEN_PREFIX = "Bearer ";

	/**
	 * 令牌前缀
	 */
	public static final String LOGIN_USER_KEY = "login_user_key";

	/**
	 * 用户ID
	 */
	public static final String JWT_USERID = "userid";

	/**
	 * 用户名称
	 */
	public static final String JWT_USERNAME = Claims.SUBJECT;

	/**
	 * 用户头像
	 */
	public static final String JWT_AVATAR = "avatar";

	/**
	 * 创建时间
	 */
	public static final String JWT_CREATED = "created";

	/**
	 * 用户权限
	 */
	public static final String JWT_AUTHORITIES = "authorities";

	/**
	 * 资源映射路径 前缀
	 */
	public static final String RESOURCE_PREFIX = "/profile";

	/**
	 * RMI 远程方法调用
	 */
	public static final String LOOKUP_RMI = "rmi:";

	/**
	 * LDAP 远程方法调用
	 */
	public static final String LOOKUP_LDAP = "ldap:";

	/**
	 * LDAPS 远程方法调用
	 */
	public static final String LOOKUP_LDAPS = "ldaps:";

	/**
	 * 定时任务白名单配置（仅允许访问的包名，如其他需要可以自行添加）
	 */
	public static final String[] JOB_WHITELIST_STR = {"com.xms"};

	/**
	 * 定时任务违规的字符
	 */
	public static final String[] JOB_ERROR_STR = {"java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
		"org.springframework", "org.apache", "com.xms.core.utils.file", "com.xms.core.config"};


	public static final String LOGIN_APP_USER_KEY = "login_app_user_key";


	/**
	 * 令牌前缀
	 */
	public static final String TOKEN_APP_PREFIX = "App ";


	/**
	 * Spring 应用名 prop key
	 */
	public static final String SPRING_APP_NAME_KEY = "spring.application.name";

	/**
	 * The "active profiles" property name.
	 */
	public static final String ACTIVE_PROFILES_PROPERTY = "spring.profiles.active";
	public static final String ACTIVE_PROPERTY_PROD = "prod";
	public static final String ACTIVE_PROPERTY_DEV = "dev";
	public static final String ACTIVE_PROPERTY_TEST = "test";
	public static final String OPRER_NAME = "admin";
	public static final Integer THIRTY_ONE = 31;
	public static final Integer ONE = 0;

	public static final String CLIENT_ID = "client_id ";
	public static final String CLIENT_ID_TO = "clientId";
	public static final String TOKEN_CODE = "token ";
	public static final String LOGIN_TYPE = "app";
}
