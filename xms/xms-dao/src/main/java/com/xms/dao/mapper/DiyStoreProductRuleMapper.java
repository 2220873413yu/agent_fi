package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.DiyStoreProductRule;

/**
 * 商品规则值Mapper接口
 *
 * @author xms
 * @date 2026-04-08
 */
public interface DiyStoreProductRuleMapper extends XmsMapper<DiyStoreProductRule>
{
    /**
     * 查询商品规则值列表
     *
     * @param diyStoreProductRule 商品规则值
     * @return 商品规则值集合
     */
    public List<DiyStoreProductRule> selectDiyStoreProductRuleList(DiyStoreProductRule diyStoreProductRule);

}
