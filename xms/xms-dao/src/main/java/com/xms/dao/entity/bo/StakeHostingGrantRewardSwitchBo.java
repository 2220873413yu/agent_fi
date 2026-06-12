package com.xms.dao.entity.bo;

import lombok.Data;

/**
 * 后台拨付托管订单收益开关请求。
 */
@Data
public class StakeHostingGrantRewardSwitchBo {
	/**
	 * 托管订单ID。
	 */
	private Long orderId;

	/**
	 * 后台拨付托管收益开关 0:关闭 1:开启。
	 */
	private Integer grantRewardEnabled;
}
