package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.DiyStoreProductMapper;
import com.xms.dao.domain.DiyStoreProduct;
import com.xms.dao.service.IDiyStoreProductService;

/**
 * 商品Service业务层处理
 *
 * @author xms
 * @date 2026-04-08
 */
@Service
public class DiyStoreProductServiceImpl extends XmsDataServiceImpl<DiyStoreProductMapper, DiyStoreProduct> implements IDiyStoreProductService
{


    /**
     * 查询商品列表
     *
     *
     * @param diyStoreProduct 商品
     * @return 商品
     */
    @Override
    public List<DiyStoreProduct> selectDiyStoreProductList(DiyStoreProduct diyStoreProduct)
    {
        return baseMapper.selectDiyStoreProductList(diyStoreProduct);
    }

}
