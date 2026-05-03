package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.DiyStoreProductAttrMapper;
import com.xms.dao.domain.DiyStoreProductAttr;
import com.xms.dao.service.IDiyStoreProductAttrService;

/**
 * 商品属性Service业务层处理
 *
 * @author xms
 * @date 2026-04-08
 */
@Service
public class DiyStoreProductAttrServiceImpl extends XmsDataServiceImpl<DiyStoreProductAttrMapper, DiyStoreProductAttr> implements IDiyStoreProductAttrService
{


    /**
     * 查询商品属性列表
     *
     *
     * @param diyStoreProductAttr 商品属性
     * @return 商品属性
     */
    @Override
    public List<DiyStoreProductAttr> selectDiyStoreProductAttrList(DiyStoreProductAttr diyStoreProductAttr)
    {
        return baseMapper.selectDiyStoreProductAttrList(diyStoreProductAttr);
    }

}
