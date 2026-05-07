package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.xms.common.exception.ServiceException;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.UserLevelConfigMapper;
import com.xms.dao.domain.UserLevelConfig;
import com.xms.dao.service.IUserLevelConfigService;

/**
 * 用户等级考核配置Service业务层处理
 *
 * @author xms
 * @date 2025-12-03
 */
@Service
public class UserLevelConfigServiceImpl extends XmsDataServiceImpl<UserLevelConfigMapper, UserLevelConfig> implements IUserLevelConfigService
{


    /**
     * 查询用户等级考核配置列表
     *
     *
     * @param userLevelConfig 用户等级考核配置
     * @return 用户等级考核配置
     */
    @Override
    public List<UserLevelConfig> selectUserLevelConfigList(UserLevelConfig userLevelConfig)
    {
        return baseMapper.selectUserLevelConfigList(userLevelConfig);
    }

	@Override
	public int updateRecordById(UserLevelConfig req) {
		if (req == null || req.getId() == null) {
			throw new ServiceException("等级配置ID不能为空");
		}
		if (req.getPerformance() == null || req.getPerformance().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("个人托管业绩不能小于等于0");
		}
		if (req.getCommunityPerformance() == null || req.getCommunityPerformance().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("小区托管业绩不能小于等于0");
		}
		if (req.getTeamRewardRatio() == null || req.getTeamRewardRatio().compareTo(BigDecimal.ZERO) < 0) {
			throw new ServiceException("团队奖励比例不能小于0");
		}
		if (req.getGlobalFeeDividendRatio() == null || req.getGlobalFeeDividendRatio().compareTo(BigDecimal.ZERO) < 0) {
			throw new ServiceException("全球手续费分红比例不能小于0");
		}
		UserLevelConfig current = getById(req.getId());
		if (current == null || current.getLevel() == null || current.getLevel() <= 0) {
			throw new ServiceException("等级配置不存在");
		}
		List<UserLevelConfig> configs = lambdaQuery()
			.gt(UserLevelConfig::getLevel, 0)
			.orderByAsc(UserLevelConfig::getLevel)
			.list();
		for (UserLevelConfig config : configs) {
			if (config.getId().equals(req.getId())) {
				config.setPerformance(req.getPerformance());
				config.setCommunityPerformance(req.getCommunityPerformance());
				config.setTeamRewardRatio(req.getTeamRewardRatio());
				config.setGlobalFeeDividendRatio(req.getGlobalFeeDividendRatio());
				break;
			}
		}
		validateLevelThreshold(configs);
		lambdaUpdate()
			.eq(UserLevelConfig::getId, req.getId())
			.set(UserLevelConfig::getPerformance, req.getPerformance())
			.set(UserLevelConfig::getCommunityPerformance, req.getCommunityPerformance())
			.set(UserLevelConfig::getTeamRewardRatio, req.getTeamRewardRatio())
			.set(UserLevelConfig::getGlobalFeeDividendRatio, req.getGlobalFeeDividendRatio())
			.set(UserLevelConfig::getUpdateTime, new Date())
			.update();
		return 1;
	}

	private void validateLevelThreshold(List<UserLevelConfig> configs) {
		for (int i = 1; i < configs.size(); i++) {
			UserLevelConfig prev = configs.get(i - 1);
			UserLevelConfig current = configs.get(i);
			if (defaultAmount(current.getPerformance()).compareTo(defaultAmount(prev.getPerformance())) < 0) {
				throw new ServiceException(levelName(current.getLevel()) + "的个人托管业绩不能小于" + levelName(prev.getLevel()));
			}
			if (defaultAmount(current.getCommunityPerformance()).compareTo(defaultAmount(prev.getCommunityPerformance())) < 0) {
				throw new ServiceException(levelName(current.getLevel()) + "的小区托管业绩不能小于" + levelName(prev.getLevel()));
			}
		}
	}

	private BigDecimal defaultAmount(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount;
	}

	private String levelName(Integer level) {
		return level == null || level <= 0 ? "暂无" : "F" + level;
	}
}
