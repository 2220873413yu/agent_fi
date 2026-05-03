package com.xms.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.DiyStoreProductAttrValue;

/**
 * 商品属性值(SKU)Mapper接口
 *
 * @author xms
 * @date 2026-04-08
 */
public interface DiyStoreProductAttrValueMapper extends XmsMapper<DiyStoreProductAttrValue>
{
    /**
     * 查询商品属性值(SKU)列表
     *
     * @param diyStoreProductAttrValue 商品属性值(SKU)
     * @return 商品属性值(SKU)集合
     */
    public List<DiyStoreProductAttrValue> selectDiyStoreProductAttrValueList(DiyStoreProductAttrValue diyStoreProductAttrValue);

	/**
	 * 按商品物理删除 SKU（避免 @TableLogic 逻辑删后唯一键冲突）
	 */
	int deletePhysicalByProductId(@Param("productId") String productId);

}
