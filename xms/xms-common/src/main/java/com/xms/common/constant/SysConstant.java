package com.xms.common.constant;

import java.math.BigDecimal;

/**
 * @author: renengadePISTA
 * @createDate: 2023/5/17
 */
public interface SysConstant {

	/**
	 * xms币种类型 字符串版
	 */
	String xms = "xms";


	/**
	 * 代码部署于 linux 上，工作默认为 mac 和 Windows
	 */
	String OS_NAME_LINUX = "LINUX";
	/**
	 * 代码部署于 linux 上，工作默认为 mac 和 Windows
	 */
	String OS_NAME_WINDOWS = "WINDOWS";
	/**
	 * 分布式锁前缀
	 */
	String REDIS_LOCK = "renegade:lock:";

	/**
	 * 枚举值
	 */
	Integer ZERO = 0;
	/**
	 * Long枚举值
	 */
	Long ZERO_LONG = 0L;
	/**
	 * 枚举值
	 */
	Integer ONE = 1;
	Long ONE_LONG = 1L;
	Long TWO_LONG = 2L;
	Long FIVE_LONG = 5L;
	Long SIX_LONG = 6L;
	Long SEVEN_LONG = 7L;
	Long THIRTY_LONG = 30L;
	/**
	 * 枚举值
	 */
	Integer TWO = 2;
	/**
	 * 枚举值
	 */
	Integer THREE = 3;
	Integer FOUR = 4;
	Integer FIVE = 5;
	Integer SIX = 6;
	Integer SEVEN = 7;
	Integer EIGHT = 8;
	Integer NINE = 9;
	Integer TEN = 10;
	Integer ELEVEN = 11;
	Integer TWELVE = 12;
	Integer THIRTEEN = 13;
	Integer FOURTEEN = 14;
	Integer FIFTEEN = 15;
	Integer SIXTEEN = 16;
	Integer SEVENTEEN = 17;
	Integer EIGHTEEN = 18;
	Integer NINETEEN = 19;
	Integer TWENTY = 20;
	Integer TWENTY_ONE = 21;
	Integer TWENTY_TWO = 22;
	Integer TWENTY_THREE = 23;
	Integer TWENTY_FOUR = 24;
	Integer TWENTY_FIVE = 25;
	Integer TWENTY_SIX = 26;
	Integer TWENTY_SEVEN = 27;
	Integer TWENTY_EIGHT = 28;
	Integer TWENTY_NINE = 29;
	Integer THIRTY = 30;
	Integer THIRTY_ONE = 31;
	Integer ONE_HUNDRED = 100;
	Integer TWO_HUNDRED = 200;
	Integer EIGHT_HUNDRED = 800;
	Integer DEFAULT_YEAR = 365;


	String CORRELATION_ID = "correlationId";
	/**
	 * 提现最小数量 U
	 */
	String WITHDRAWAL_MIN = "WITHDRAWAL_MIN";

	/**
	 * 提现最小数量 ALEO
	 */
	String WITHDRAWAL_ALEO_MIN = "WITHDRAWAL_ALEO_MIN";

	/**
	 * lamada   默认分页 20条
	 */
	String PAGE_LIMIT = "limit 20";

	/**
	 * lamada   默认分页 5条
	 */
	String PAGE_LIMIT_5 = "limit 5";

	/**
	 * lamada   默认分页 10条
	 */
	String PAGE_LIMIT_10 = "limit 10";

	/**
	 * 单位分钟
	 */
	long IOD_ORDER_FAIL_MINUTES =720L;

	BigDecimal BAIFENBI = new BigDecimal(SysConstant.ONE_HUNDRED);
	String ONE_LIMIT = "limit 1";
	/**
	 * 每日动态+静态结算在此时间范围内允许，不允许交易
	 */
	String MINE_TRADE_TIME_LIMIT ="mine_trade_time_limit" ;

	/**
	 * 每日利息包结算在此时间范围内允许，不允许交易
	 */
	String MINE_TRADE_TIME_LIMIT1 ="mine_trade_time_limit1" ;
	/**
	 * 循环分片处理
	 */
	String SHARDING_LIMIT = "limit 3000";
	/**
	 * 全网能量池分红数量
	 */
	String ALL_SHARE_NUM = "all_share_num";
	String U_TO_ALEO = "u_to_aleo";

	/**
	 * 万能码开关 0 关 1开
	 */
	String VERIFY_CODE_OFF = "verify_code_off";

	/**
	 * 万能码
	 */
	String VERIFY_CODE = "verify_code";

	/**
	 * 批量插入 默认1000条
	 */
	Integer DEFAULT_SQL_NATIVE_BATCH_SIZE = 1000;

	/**
	 * 任务类型 100:定期根据可用valid_num1余额释放收益
	 */
	String TSK_TYPE_100 = "100";

	/**
	 * 任务类型101 每天释放债券套餐订单,产出ptb
	 */
	String TSK_TYPE_101 = "101";

	/**
	 * 任务类型102 每天释放矿机订单,产出usdt
	 */
	String TSK_TYPE_102 = "102";

	/**
	 *任务类型103 复利手续费分红
	 */
	String TSK_TYPE_103 = "103";

	/**
	 * 任务类型104 卖币手续费分红
	 */
	String TSK_TYPE_104 = "104";

	/**
	 * 任务类型105 债券订单分红的利息分红
	 */
	String TSK_TYPE_105 = "105";

	/**
	 * 任务类型200 节点认购AFI每日线性释放
	 */
	String TSK_TYPE_200 = "200";


	String CALLBACK_SUCCESS = "success";
	String CALLBACK_FAIL = "fail";
}
