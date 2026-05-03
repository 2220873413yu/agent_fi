package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.MiningMgmtFeeConfigMapper;
import com.xms.dao.domain.MiningMgmtFeeConfig;
import com.xms.dao.service.IMiningMgmtFeeConfigService;

/**
 * 矿机管理费配置Service业务层处理
 *
 * @author xms
 * @date 2026-02-27
 */
@Service
public class MiningMgmtFeeConfigServiceImpl extends XmsDataServiceImpl<MiningMgmtFeeConfigMapper, MiningMgmtFeeConfig> implements IMiningMgmtFeeConfigService
{


    /**
     * 查询矿机管理费配置列表
     *
     *
     * @param miningMgmtFeeConfig 矿机管理费配置
     * @return 矿机管理费配置
     */
    @Override
    public List<MiningMgmtFeeConfig> selectMiningMgmtFeeConfigList(MiningMgmtFeeConfig miningMgmtFeeConfig)
    {
        return baseMapper.selectMiningMgmtFeeConfigList(miningMgmtFeeConfig);
    }

}
