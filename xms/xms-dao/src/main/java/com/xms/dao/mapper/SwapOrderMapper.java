package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.SwapOrder;

/**
 * swap订单Mapper接口
 *
 * @author xms
 * @date 2026-01-04
 */
public interface SwapOrderMapper extends XmsMapper<SwapOrder>
{
    /**
     * 查询swap订单列表
     *
     * @param swapOrder swap订单
     * @return swap订单集合
     */
    public List<SwapOrder> selectSwapOrderList(SwapOrder swapOrder);

}
