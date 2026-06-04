package com.xms.dao.entity.bo;

import lombok.Data;

/**
 * 后台拨付托管收益用户开关请求。
 */
@Data
public class GrantHostingRewardSwitchBo {
	/**
	 * 用户ID
	 */
	private Long userId;

	/**
	 * 后台拨付托管收益开关 0:关闭 1:开启
	 */
	private Integer enabled;
}
