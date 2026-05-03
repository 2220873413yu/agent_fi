package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.StakeProductMapper;
import com.xms.dao.domain.StakeProduct;
import com.xms.dao.service.IStakeProductService;

/**
 * 质押套餐Service业务层处理
 *
 * @author xms
 * @date 2026-03-09
 */
@Service
public class StakeProductServiceImpl extends XmsDataServiceImpl<StakeProductMapper, StakeProduct> implements IStakeProductService
{


    /**
     * 查询质押套餐列表
     *
     *
     * @param stakeProduct 质押套餐
     * @return 质押套餐
     */
    @Override
    public List<StakeProduct> selectStakeProductList(StakeProduct stakeProduct)
    {
        return baseMapper.selectStakeProductList(stakeProduct);
    }

}
