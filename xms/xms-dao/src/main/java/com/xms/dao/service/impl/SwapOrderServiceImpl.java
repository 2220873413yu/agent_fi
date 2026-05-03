package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.SwapOrderMapper;
import com.xms.dao.domain.SwapOrder;
import com.xms.dao.service.ISwapOrderService;

/**
 * swap订单Service业务层处理
 *
 * @author xms
 * @date 2026-01-04
 */
@Service
public class SwapOrderServiceImpl extends XmsDataServiceImpl<SwapOrderMapper, SwapOrder> implements ISwapOrderService
{


    /**
     * 查询swap订单列表
     *
     *
     * @param swapOrder swap订单
     * @return swap订单
     */
    @Override
    public List<SwapOrder> selectSwapOrderList(SwapOrder swapOrder)
    {
        return baseMapper.selectSwapOrderList(swapOrder);
    }

}
