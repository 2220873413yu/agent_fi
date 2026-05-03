package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.MiningMgmtFeeConfig;

/**
 * 矿机管理费配置Service接口
 *
 * @author xms
 * @date 2026-02-27
 */
public interface IMiningMgmtFeeConfigService extends XmsDataService<MiningMgmtFeeConfig>
{

    /**
     * 查询矿机管理费配置列表
     *
     * @param miningMgmtFeeConfig 矿机管理费配置
     * @return 矿机管理费配置集合
     */
    public List<MiningMgmtFeeConfig> selectMiningMgmtFeeConfigList(MiningMgmtFeeConfig miningMgmtFeeConfig);

}
