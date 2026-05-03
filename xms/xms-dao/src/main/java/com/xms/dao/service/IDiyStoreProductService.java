package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.DiyStoreProduct;

/**
 * 商品Service接口
 *
 * @author xms
 * @date 2026-04-08
 */
public interface IDiyStoreProductService extends XmsDataService<DiyStoreProduct>
{

    /**
     * 查询商品列表
     *
     * @param diyStoreProduct 商品
     * @return 商品集合
     */
    public List<DiyStoreProduct> selectDiyStoreProductList(DiyStoreProduct diyStoreProduct);

}
