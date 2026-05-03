package com.xms.common.constant;

import java.math.BigDecimal;
import java.math.RoundingMode;

/***
 * 常量类
 */
public class ConstantStatic {

	//万能验证码
	public static final String VERIFICATION_CODE = "888888";

	public static final String MD5_KEY = "471dfd7723d1442285cd2511d0dff193";

	public static final String NULL = "null";
	/**
	 * 默认版本
	 */
	public static final Integer VERSION = 1;

	public static final String LIMIT_ONE = "LIMIT 1";

	public static final String ACTIVE_FLAG = " active_flag = 1 ";

	public static final String VERSION_UPDATE = " version = version + 1";

	//资金小数保留位数
	public static final Integer newScale = 6;

	//资金小数保留2位数
	public static final Integer twoScale = 2;

	//资金小数保留0位数
	public static final Integer zeroScale = 0;

	//资金小数保留8位数
	public static final Integer eightScale = 8;
	//舍掉
	//public static final int roundingMode = BigDecimal.ROUND_DOWN;
	public static final RoundingMode roundingModeNew = RoundingMode.DOWN;


	//七牛云token
	public static final String QINIU_TOKE = "QINIU:TOKEN:";

	/**
	 * 代码部署于 linux 上，工作默认为 mac 和 Windows
	 */
	public static final String OS_NAME_LINUX = "LINUX";
	/**
	 * 代码部署于 linux 上，工作默认为 mac 和 Windows
	 */
	public static final String OS_NAME_WINDOWS = "WINDOWS";

	//用户随机数
	public static final String USER_RANDOM = RedisConstant.REDIS_PREFIX + "USER:RANDOM:";

	//用户状态
	public static final String USER_STATUS = RedisConstant.REDIS_PREFIX + "USER:STATUS:";

	//市场交易
	public static final String market_trade = RedisConstant.REDIS_PREFIX + "market:trade:";

	//币种图标
	public static final String market_icon = RedisConstant.REDIS_PREFIX + "market:icon:";

	//每日基金结算价格
	public static final String settlement_sgm_price = RedisConstant.REDIS_PREFIX + "settlement:sgm:price";
}
