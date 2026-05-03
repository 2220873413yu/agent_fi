package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.DiyStoreProductRuleMapper;
import com.xms.dao.domain.DiyStoreProductRule;
import com.xms.dao.service.IDiyStoreProductRuleService;

/**
 * 商品规则值Service业务层处理
 *
 * @author xms
 * @date 2026-04-08
 */
@Service
public class DiyStoreProductRuleServiceImpl extends XmsDataServiceImpl<DiyStoreProductRuleMapper, DiyStoreProductRule> implements IDiyStoreProductRuleService
{


    /**
     * 查询商品规则值列表
     *
     *
     * @param diyStoreProductRule 商品规则值
     * @return 商品规则值
     */
    @Override
    public List<DiyStoreProductRule> selectDiyStoreProductRuleList(DiyStoreProductRule diyStoreProductRule)
    {
        return baseMapper.selectDiyStoreProductRuleList(diyStoreProductRule);
    }

}
