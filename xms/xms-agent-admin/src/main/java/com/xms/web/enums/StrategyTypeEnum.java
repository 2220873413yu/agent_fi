package com.xms.web.enums;

import lombok.Getter;

/**
 * 异步执行枚举
 *
 * @author MIER
 */
@Getter
public enum StrategyTypeEnum {


	/**
	 * 默认的
	 */
	NPE_STRATEGY("常规空指针判定"),
	WITHDRAWAL_OPEN_OR_CLOSE("只能填写1和2"),
	WITHDRAWAL_RATIO("只能在0-1之间/非空"),
	all_share_num("正数判定/非空"),
	WITHDRAWAL_MIN("正数判定/非空"),
	mine_trade_time_limit("格式校验/非空"),
	;

	/**
	 * 描述
	 */
	private final String desc;

	StrategyTypeEnum(String desc) {
		this.desc = desc;
	}

	public static String getDesc(String type) {
		for (StrategyTypeEnum typeEnum : StrategyTypeEnum.values()) {
			if (typeEnum.name().equals(type)) {
				return typeEnum.getDesc();
			}
		}
		return null;
	}

}
