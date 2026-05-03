package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.DiyStoreProductAttrValueMapper;
import com.xms.dao.domain.DiyStoreProductAttrValue;
import com.xms.dao.service.IDiyStoreProductAttrValueService;

/**
 * 商品属性值(SKU)Service业务层处理
 *
 * @author xms
 * @date 2026-04-08
 */
@Service
public class DiyStoreProductAttrValueServiceImpl extends XmsDataServiceImpl<DiyStoreProductAttrValueMapper, DiyStoreProductAttrValue> implements IDiyStoreProductAttrValueService
{


    /**
     * 查询商品属性值(SKU)列表
     *
     *
     * @param diyStoreProductAttrValue 商品属性值(SKU)
     * @return 商品属性值(SKU)
     */
    @Override
    public List<DiyStoreProductAttrValue> selectDiyStoreProductAttrValueList(DiyStoreProductAttrValue diyStoreProductAttrValue)
    {
        return baseMapper.selectDiyStoreProductAttrValueList(diyStoreProductAttrValue);
    }

}
