package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.StakeOrder;
import com.xms.dao.entity.dto.StakeOrderListDto;

/**
 * 质押订单Mapper接口
 *
 * @author xms
 * @date 2026-02-27
 */
public interface StakeOrderMapper extends XmsMapper<StakeOrder>
{
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

}
