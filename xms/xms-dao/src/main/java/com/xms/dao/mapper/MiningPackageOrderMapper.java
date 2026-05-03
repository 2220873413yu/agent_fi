package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.MiningPackageOrder;

/**
 * 矿机订单Mapper接口
 *
 * @author xms
 * @date 2026-02-23
 */
public interface MiningPackageOrderMapper extends XmsMapper<MiningPackageOrder>
{
    /**
     * 查询矿机订单列表
     *
     * @param miningPackageOrder 矿机订单
     * @return 矿机订单集合
     */
    public List<MiningPackageOrder> selectMiningPackageOrderList(MiningPackageOrder miningPackageOrder);

}
