package com.xms.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.DiyStoreProductAttr;

/**
 * 商品属性Mapper接口
 *
 * @author xms
 * @date 2026-04-08
 */
public interface DiyStoreProductAttrMapper extends XmsMapper<DiyStoreProductAttr>
{
    /**
     * 查询商品属性列表
     *
     * @param diyStoreProductAttr 商品属性
     * @return 商品属性集合
     */
    public List<DiyStoreProductAttr> selectDiyStoreProductAttrList(DiyStoreProductAttr diyStoreProductAttr);

	/**
	 * 按商品物理删除属性（避免 @TableLogic 逻辑删后唯一键冲突）
	 */
	int deletePhysicalByProductId(@Param("productId") String productId);

}
