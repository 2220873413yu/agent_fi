package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.xms.common.exception.ServiceException;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.MiningRewardConfigMapper;
import com.xms.dao.domain.MiningRewardConfig;
import com.xms.dao.service.IMiningRewardConfigService;

/**
 * 矿机奖励分配配置Service业务层处理
 *
 * @author xms
 * @date 2026-02-25
 */
@Service
public class MiningRewardConfigServiceImpl extends XmsDataServiceImpl<MiningRewardConfigMapper, MiningRewardConfig> implements IMiningRewardConfigService
{


    /**
     * 查询矿机奖励分配配置列表
     *
     *
     * @param miningRewardConfig 矿机奖励分配配置
     * @return 矿机奖励分配配置
     */
    @Override
    public List<MiningRewardConfig> selectMiningRewardConfigList(MiningRewardConfig miningRewardConfig)
    {
        return baseMapper.selectMiningRewardConfigList(miningRewardConfig);
    }


	@Override
	public int updateConfigById(MiningRewardConfig miningRewardConfig) {
		if(miningRewardConfig.getRewardRatio() == null || miningRewardConfig.getRewardRatio().compareTo(BigDecimal.ZERO) < 0){
			throw new ServiceException("奖励比例不能小于0");
		}

		lambdaUpdate()
			.eq(MiningRewardConfig::getId, miningRewardConfig.getId())
			.set(MiningRewardConfig::getRewardRatio, miningRewardConfig.getRewardRatio())
			.set(MiningRewardConfig::getUpdateTime, new Date())
			.set(MiningRewardConfig::getUpdateBy, miningRewardConfig.getUpdateBy())
			.update();
		return 1;
	}
}
