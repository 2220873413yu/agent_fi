package com.xms.common.constant;

/**
 * 三方外部API
 *
 * @author: GT63S
 * @createDate: 2025/7/25
 */
public class ExternalApiConstant {
	/**
	 * 通过code换取授权access_token
	 */
	public static final String JU_EXCHANGE = "juExchange";
	public static final String JU_EXCHANGE_BASE_URL = JU_EXCHANGE + ".url";
	public static final String JU_EXCHANGE_APPID = JU_EXCHANGE + ".appId";
	public static final String JU_EXCHANGE_APP_SECRET = JU_EXCHANGE + ".appSecret";
	public static final String ACCESS_TOKEN_URL = "/bp/third/open/oauth2/access_token?appId={0}&appSecret={1}&code={2}";
	/**
	 * 用户 划转至 应用/三方 充
	 */
	public static final String USER_TO_APPLICATION = "user_to_application";
	/**
	 * 三方/应用 划转至 用户 提
	 */
	public static final String APPLICATION_TO_USER = "application_to_user";

	/**
	 * 提现申请
	 */
	//public static final String WDT_APPLY_URL = "/bp/third/open/transfer/out?openId={0}";
	public static final String WDT_APPLY_URL = "/bp/act/public/application/third/transfer-apply?openId={0}";

}
