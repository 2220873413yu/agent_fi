package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.DiyStoreProductAttrValue;

/**
 * 商品属性值(SKU)Service接口
 *
 * @author xms
 * @date 2026-04-08
 */
public interface IDiyStoreProductAttrValueService extends XmsDataService<DiyStoreProductAttrValue>
{

    /**
     * 查询商品属性值(SKU)列表
     *
     * @param diyStoreProductAttrValue 商品属性值(SKU)
     * @return 商品属性值(SKU)集合
     */
    public List<DiyStoreProductAttrValue> selectDiyStoreProductAttrValueList(DiyStoreProductAttrValue diyStoreProductAttrValue);

}
