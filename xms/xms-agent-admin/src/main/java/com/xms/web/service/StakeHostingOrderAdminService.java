package com.xms.web.service;

import com.xms.dao.entity.bo.StakeHostingGrantRewardSwitchBo;

/**
 * 后台托管订单操作服务。
 */
public interface StakeHostingOrderAdminService {
	/**
	 * 后台取消运行中的托管订单。
	 *
	 * @param orderId 托管订单ID
	 * @return 1表示取消成功或已完成幂等成功
	 */
	int cancelHostingOrder(Long orderId);

	/**
	 * 修改后台拨付托管订单收益开关。
	 *
	 * @param req 开关请求
	 * @return 1表示修改成功
	 */
	int updateGrantRewardSwitch(StakeHostingGrantRewardSwitchBo req);
}
