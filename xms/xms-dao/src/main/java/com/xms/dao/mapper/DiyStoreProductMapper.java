package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.DiyStoreProduct;

/**
 * 商品Mapper接口
 *
 * @author xms
 * @date 2026-04-08
 */
public interface DiyStoreProductMapper extends XmsMapper<DiyStoreProduct>
{
    /**
     * 查询商品列表
     *
     * @param diyStoreProduct 商品
     * @return 商品集合
     */
    public List<DiyStoreProduct> selectDiyStoreProductList(DiyStoreProduct diyStoreProduct);

}
