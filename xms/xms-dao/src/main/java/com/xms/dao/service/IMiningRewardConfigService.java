package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.MiningRewardConfig;

/**
 * 矿机奖励分配配置Service接口
 *
 * @author xms
 * @date 2026-02-25
 */
public interface IMiningRewardConfigService extends XmsDataService<MiningRewardConfig>
{

    /**
     * 查询矿机奖励分配配置列表
     *
     * @param miningRewardConfig 矿机奖励分配配置
     * @return 矿机奖励分配配置集合
     */
    public List<MiningRewardConfig> selectMiningRewardConfigList(MiningRewardConfig miningRewardConfig);

	int updateConfigById(MiningRewardConfig miningRewardConfig);
}
