package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.StakeProduct;

/**
 * 质押套餐Service接口
 *
 * @author xms
 * @date 2026-02-27
 */
public interface IStakeProductService extends XmsDataService<StakeProduct>
{

    /**
     * 查询质押套餐列表
     *
     * @param stakeProduct 质押套餐
     * @return 质押套餐集合
     */
    public List<StakeProduct> selectStakeProductList(StakeProduct stakeProduct);

}
