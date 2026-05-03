package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.StakeOrder;

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

}
