package com.xms.dao.service;

import java.util.List;

import com.xms.dao.domain.UserLevelConfig;
import com.xms.dao.entity.dto.StakeOrderListDto;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.StakeOrder;

/**
 * 质押订单Service接口
 *
 * @author xms
 * @date 2026-02-27
 */
public interface IStakeOrderService extends XmsDataService<StakeOrder>
{

	/**
	 * 计算用户质押等级
	 * @param info
	 * @param userLevelConfigList
	 */
	public void callUserLevel(UserInfo info, List<UserLevelConfig> userLevelConfigList);


	/**
	 * 计算小区+大区业绩
	 * @param parentIds
	 */
	public void calculateCommunityPerformance(List<Long> parentIds);
    /**
     * 查询质押订单列表
     *
     * @param stakeOrder 质押订单
     * @return 质押订单集合
     */
    public List<StakeOrder> selectStakeOrderList(StakeOrder stakeOrder);

	/**
	 * 查询后台质押订单列表展示数据。
	 *
	 * @param query 查询条件
	 * @return 后台质押订单列表，包含AFI加速倍率展示字段
	 */
	public List<StakeOrderListDto> selectStakeOrderDtoList(StakeOrderListDto query);

	/**
	 * 手动修改质押订单
	 * @param stakeOrder
	 * @return
	 */
    int updateStakeOrderById(StakeOrder stakeOrder);

	/**
	 * 下架质押订单
	 * @param stakeOrder
	 * @return
	 */
	int disableStakeOrder(StakeOrder stakeOrder);

	/**
	 * 质押订单发货
	 * @param stakeOrder
	 * @return
	 */
	int orderShipped(StakeOrder stakeOrder);
}
