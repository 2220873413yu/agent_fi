package com.xms.common.constant;

/**
 *
  * @ClassName: ConstantSys
  * @Description: 系统参数
  *
  * @date 2023年5月23日 下午3:26:14
  *
 */
public class ConstantSys {
	//USDT闪兑平台币手续费率 例如:写1 手续费就是1%
	public static final String biz_swap_usdt_to_p_fee_ratio = "biz_swap_usdt_to_p_fee_ratio";

	//平台币闪兑USDT手续费率 例如:写1 手续费就是1%
	public static final String biz_swap_p_to_usdt_fee_ratio = "biz_swap_p_to_usdt_fee_ratio";

	//平台币价格 默认为1.2
	public static final String biz_p_price = "biz_p_price";

	//图片域名
	public static final String biz_image_domain = "biz_image_domain";

	//USDT兑换成SMA开关 例如:1开,其他值是关
	public static final String biz_usdt_to_sgm_enable = "biz_usdt_to_sgm_enable";

	//提币手续费中，用于 V9 分红奖励 的比例 例如：5 表示 5%
	public static final String biz_withdrawal_fee_v9_reward_ratio = "biz_withdrawal_fee_v9_reward_ratio";

	//提币手续费中，用于 平台项目方收益 的比例 例如：5 表示 5%
	public static final String biz_withdrawal_fee_platform_income_ratio = "biz_withdrawal_fee_platform_income_ratio";

	//手续费分红 平台收益（分发到项目方钱包）会分发到该地址
	public static final String biz_sk_address = "biz_sk_address";

	//直推奖励 算力奖励比例 例如:10表示10%
	public static final String biz_direct_computing_power_ratio = "biz_direct_computing_power_ratio";


	//间推奖励 算力奖励比例 例如:5表示5%
	public static final String biz_indirect_computing_power_ratio = "biz_indirect_computing_power_ratio";

	/**
	 * 激活一次赠送的领取空投次数 例如:15 激活一次赠送15次
	 */
	public static final String biz_send_activation_count = "biz_send_activation_count";

	/**
	 * 激活一次需要支付的金额（与空投赠送次数的规则配套）
	 */
	public static final String biz_activation_cost = "biz_activation_cost";

	//直推奖励 u奖励比例 例如:10表示10%
	public static final String biz_direct_xls_ratio = "biz_direct_xls_ratio";

	//间推奖励 u奖励比例 例如:10表示10%
	public static final String biz_indirect_xls_ratio = "biz_indirect_xls_ratio";

	//swap订单额度生效时间 默认86400秒 约等于1天
	public static final String biz_swap_order_expire_time = "biz_swap_order_expire_time";


	//swap订单生效额度比例 例如：100 填写100 表示swap订单生效额度为swap订单额度的100%
	public static final String biz_swap_order_effective_ratio = "biz_swap_order_effective_ratio";

	//新增的

	//节点订单超时时间 默认5分钟 单位为分钟(废弃)
	public static final String biz_lock_expire_at = "biz_lock_expire_at";

	//平台手续费分红 例如20就是20%(废弃)
	public static final String biz_fee_dividend_rate = "biz_fee_dividend_rate";

	//商城利润分红 例如10就是10%(废弃)
	public static final String biz_mall_profit_dividend_rate = "biz_mall_profit_dividend_rate";


	//新增
	//USDT转账开关(1:开,其他值关) 例如: 填1就是打开转账功能
	public static final String biz_transfer_usdt_enable = "biz_transfer_usdt_enable";

	//DFC转账开关(1:开,其他值关) 例如: 填1就是打开转账功能
	public static final String biz_transfer_dfc_enable = "biz_transfer_dfc_enable";

	//OORT转账开关(1:开,其他值关) 例如: 填1就是打开转账功能
	public static final String biz_transfer_oort_enable = "biz_transfer_oort_enable";

	//锁定USDT转账开关(1:开,其他值关) 例如: 填1就是打开转账功能
	public static final String biz_transfer_sd_usdt_enable = "biz_transfer_sd_usdt_enable";

	//产出DFC转账开关(1:开,其他值关) 例如: 填1就是打开转账功能
	public static final String biz_transfer_cc_dfc_enable = "biz_transfer_cc_dfc_enable";

	//转账功能 USDT最少转账限制 例如:填写15,默认最低15起转
	public static final String biz_usdt_transfer_min_limit = "biz_usdt_transfer_min_limit";

	//转账功能 DFC最少转账限制 例如:填写15,默认最低15起转
	public static final String biz_dfc_transfer_min_limit = "biz_dfc_transfer_min_limit";

	//转账功能 OORT最少转账限制 例如:填写15,默认最低15起转
	public static final String biz_oort_transfer_min_limit = "biz_oort_transfer_min_limit";

	//转账功能 产出锁定USDT最少转账限制 例如:填写15,默认最低15起转
	public static final String biz_sd_usdt_transfer_min_limit = "biz_sd_usdt_transfer_min_limit";

	//转账功能 产出DFC最少转账限制 例如:填写15,默认最低15起转
	public static final String biz_cc_dfc_transfer_min_limit = "biz_cc_dfc_transfer_min_limit";

	//转账功能 USDT转账手续费率 例如:填写5 就是5%
	public static final String biz_usdt_transfer_fee_ratio = "biz_usdt_transfer_fee_ratio";

	//转账功能 DFC转账手续费率 例如:填写5 就是5%
	public static final String biz_dfc_transfer_fee_ratio = "biz_dfc_transfer_fee_ratio";

	//转账功能 OORT转账手续费率 例如:填写5 就是5%
	public static final String biz_oort_transfer_fee_ratio = "biz_oort_transfer_fee_ratio";
	//转账功能 锁定USDT转账手续费率 例如:填写5 就是5%
	public static final String biz_sd_usdt_transfer_fee_ratio = "biz_sd_usdt_transfer_fee_ratio";

	//转账功能 产出DFC转账手续费率 例如:填写5 就是5%
	public static final String biz_cc_dfc_transfer_fee_ratio = "biz_cc_dfc_transfer_fee_ratio";

	//质押启动期天数（质押后进入启动中的天数） 例如:写7 就是7天后启动
	public static final String biz_stake_startup_days = "biz_stake_startup_days";



	//系统沉淀手续费统计
	public static final String biz_platform_fee = "biz_platform_fee";


	//质押直推奖励 例如:7就是7%
	public static final String biz_stake_invite_ratio = "biz_stake_invite_ratio";

	//质押间推奖励 例如: 1就是1%
	public static final String biz_stake_indirect_ratio = "biz_stake_indirect_ratio";

	//托管直推奖励比例 例如:10就是10%
	public static final String biz_stake_hosting_direct_reward_ratio = "biz_stake_hosting_direct_reward_ratio";

	// 调用open ai请求支付多少钱 例如:1就是支付1AFI
	public static final String biz_pay_afi_amount = "biz_pay_afi_amount";

	/**
	 * 未触发G7区间时，未回本订单使用的纯静态基础收益率，单位%。
	 */
	public static final String PURE_STATIC_RATE_BEFORE_RETURN_PERCENT = "PURE_STATIC_RATE_BEFORE_RETURN_PERCENT";

	/**
	 * 未触发G7区间时，已回本订单使用的纯静态基础收益率，单位%。
	 */
	public static final String PURE_STATIC_RATE_AFTER_RETURN_PERCENT = "PURE_STATIC_RATE_AFTER_RETURN_PERCENT";

}
