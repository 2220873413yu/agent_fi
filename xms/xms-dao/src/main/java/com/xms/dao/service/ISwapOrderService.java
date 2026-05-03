package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.SwapOrder;

/**
 * swap订单Service接口
 *
 * @author xms
 * @date 2026-01-04
 */
public interface ISwapOrderService extends XmsDataService<SwapOrder>
{

    /**
     * 查询swap订单列表
     *
     * @param swapOrder swap订单
     * @return swap订单集合
     */
    public List<SwapOrder> selectSwapOrderList(SwapOrder swapOrder);

}
