package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.DiyStoreProductAttr;

/**
 * 商品属性Service接口
 *
 * @author xms
 * @date 2026-04-08
 */
public interface IDiyStoreProductAttrService extends XmsDataService<DiyStoreProductAttr>
{

    /**
     * 查询商品属性列表
     *
     * @param diyStoreProductAttr 商品属性
     * @return 商品属性集合
     */
    public List<DiyStoreProductAttr> selectDiyStoreProductAttrList(DiyStoreProductAttr diyStoreProductAttr);

}
