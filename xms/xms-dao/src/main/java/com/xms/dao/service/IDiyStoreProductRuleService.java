package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.DiyStoreProductRule;

/**
 * 商品规则值Service接口
 *
 * @author xms
 * @date 2026-04-08
 */
public interface IDiyStoreProductRuleService extends XmsDataService<DiyStoreProductRule>
{

    /**
     * 查询商品规则值列表
     *
     * @param diyStoreProductRule 商品规则值
     * @return 商品规则值集合
     */
    public List<DiyStoreProductRule> selectDiyStoreProductRuleList(DiyStoreProductRule diyStoreProductRule);

}
