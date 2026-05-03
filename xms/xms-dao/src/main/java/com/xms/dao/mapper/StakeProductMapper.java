package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.StakeProduct;

/**
 * 质押套餐Mapper接口
 *
 * @author xms
 * @date 2026-02-27
 */
public interface StakeProductMapper extends XmsMapper<StakeProduct>
{
    /**
     * 查询质押套餐列表
     *
     * @param stakeProduct 质押套餐
     * @return 质押套餐集合
     */
    public List<StakeProduct> selectStakeProductList(StakeProduct stakeProduct);

}
