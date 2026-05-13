package com.xms.common.constant;

/**
 *
  * @ClassName: ConstantType
  * @Description: 常量类
  *
  * @date 2023年5月23日 下午3:26:14
  *
 */
public class ConstantType {

	public static String DYNAMIC_USER_RECHARGE_TYPE = "t_recharge_coin_type";

	//开关 1-否 2-是
	public class open_or_close{
		public static final int type_1 = 1;
		public static final int type_2 = 2;
	}

	//状态(1.正常 2.冻结)
	public class user_info_status{
		public static final int type_1 = 1;
		public static final int type_2 = 2;
	}

	//等级(0.无 1.F1 2.F2 3.F3 4.F4 5.F5 6.F6 7.F7 8.F8 9.F9)
	public class user_info_game_level{
		public static final int type_0 = 0;
		public static final int type_1 = 1;
		public static final int type_2 = 2;
		public static final int type_3 = 3;
		public static final int type_4 = 4;
		public static final int type_5 = 5;
		public static final int type_6 = 6;
		public static final int type_7 = 7;
		public static final int type_8 = 8;
		public static final int type_9 = 9;
	}

	//奖励等级(1.V1 2.V2 3.V3 4.V4 5.V5 6.V6)
	public class user_level_reward_level{
		public static final int type_1 = 1;
		public static final int type_2 = 2;
		public static final int type_3 = 3;
		public static final int type_4 = 4;
		public static final int type_5 = 5;
		public static final int type_6 = 6;
	}

	//币种1:USDT,2:AFI
	public class user_money_coin_type {
		public static final int type_1 = 1;
		public static final int type_2 = 2;
		public static final int type_3 = 3;
		public static final int type_4 = 4;
		public static final int type_5 = 5;
		public static final int type_6 = 6;
		public static final int type_7 = 7;
		public static final int type_8 = 8;
		public static final int type_9 = 9;
	}

	/**
	 *
	 * 1:节点直推奖,2:节点间推奖
	 *
	 *
	 */
	public class xms_reward_record_source_type{
		public static final int type_1 = 1;
		public static final int type_2 = 2;
		public static final int type_3 = 3;
		public static final int type_4 = 4;
		public static final int type_5 = 5;
		public static final int type_6 = 6;
		public static final int type_7 = 7;
		public static final int type_8 = 8;
		public static final int type_9 = 9;
		public static final int type_10 = 10;
		public static final int type_11 = 11;
		public static final int type_12 = 12;
		public static final int type_13 = 13;
		public static final int type_14 = 14;
		public static final int type_15 = 15;
		public static final int type_16 = 16;
		public static final int type_17 = 17;
		public static final int type_18 = 18;
		public static final int type_19 = 19;
		public static final int type_20 = 20;
		public static final int type_21 = 21;
		public static final int type_22 = 22;
		public static final int type_23 = 23;
		public static final int type_24 = 24;
		public static final int type_25 = 25;
		public static final int type_26 = 26;
		//托管静态收益
		public static final int type_27 = 27;
		//托管直推奖
		public static final int type_28 = 28;
		//托管极差奖
		public static final int type_29 = 29;
		//托管平级奖
		public static final int type_30 = 30;
		//托管全球分红
		public static final int type_31 = 31;
	}

	/**
	 * 奖金业务类型 1.购买卡包,2:直推奖励(算力),3:间推奖励(算力),4:升级补算力,5:购买卡包赠送,6:升级卡包赠送
	 *
	 */
	public class xms_reward_record_business_type{
		public static final int type_1 = 1;
		public static final int type_2 = 2;
		public static final int type_3 = 3;
		public static final int type_4 = 4;
		public static final int type_5 = 5;
		public static final int type_6 = 6;
		public static final int type_7 = 7;
	}

	/**
	 * 奖金币种类型 1:算力,2:usdt,3:BDAI
	 */
	public class reward_record_coin_type{
		public static final int type_1 = 1;
		public static final int type_2 = 2;
		public static final int type_3 = 3;
	}

	/**
	 * 1:节点直推奖,2:节点间推奖,3:充值,4:提现,5:提现驳回,6:聊天扣除,28:平台拨扣
	 */
	public class user_money_log_source_type{
		public static final int type_1 = 1;
		public static final int type_2 = 2;
		public static final int type_3 = 3;
		public static final int type_4 = 4;
		public static final int type_5 = 5;
		public static final int type_6 = 6;
		public static final int type_7 = 7;
		public static final int type_8 = 8;
		public static final int type_9 = 9;
		public static final int type_10 = 10;
		public static final int type_11 = 11;
		public static final int type_12 = 12;
		public static final int type_13 = 13;
		public static final int type_14 = 14;
		public static final int type_15 = 15;
		public static final int type_16 = 16;
		public static final int type_17 = 17;
		public static final int type_18 = 18;
		public static final int type_19 = 19;

		public static final int type_20 = 20;
		public static final int type_21 = 21;
		public static final int type_22 = 22;
		public static final int type_23 = 23;
		public static final int type_24 = 24;
		public static final int type_25 = 25;
		public static final int type_26 = 26;
		public static final int type_27 = 27;
		public static final int type_28 = 28;
		public static final int type_29 = 29;
		public static final int type_30 = 30;
		//托管静态收益
		public static final int type_31 = 31;
		//托管直推奖
		public static final int type_32 = 32;
		//托管极差奖
		public static final int type_33 = 33;
		//托管平级奖
		public static final int type_34 = 34;
		//AFI质押扣减
		public static final int type_35 = 35;
		//AFI质押退还
		public static final int type_36 = 36;
		//托管全球分红
		public static final int type_37 = 37;
		//充值
		public static final int type_38 = 38;
		public static final int type_44 = 44;
	}

	//状态(0.待审核,1.审核成功,2.审核驳回,3.提现成功,4.打款失败)
	public class withdrawal_status{
		public static final int type_0 = 0;
		public static final int type_1 = 1;
		public static final int type_2 = 2;
		public static final int type_3 = 3;
		public static final int type_4 = 4;
	}
}
