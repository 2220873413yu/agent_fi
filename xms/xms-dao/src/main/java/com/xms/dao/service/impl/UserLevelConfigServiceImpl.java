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
		if(req.getLevel() == 1){
			if(req.getPerformance().compareTo(BigDecimal.ZERO)<=0){
				throw new ServiceException("个人业绩不能小于等于0");
			}
		}else if(req.getLevel() == 2){
			if(req.getTeamPerformance().compareTo(BigDecimal.ZERO)<=0){
				throw new ServiceException("大区业绩(质押量)不能小于等于0");
			}
			if(req.getCommunityPerformance().compareTo(BigDecimal.ZERO)<=0){
				throw new ServiceException("小区业绩(质押量)不能小于等于0");
			}
		}else{
			if(req.getRequiredLegNum()<=0){
				throw new ServiceException("需要满足的线数量不能小于等于0");
			}

			if(req.getLegLevelMin()<=0){
				throw new ServiceException("线内代理最小等级最少为区代理");
			}

			if(req.getLegLevelCount()<=0){
				throw new ServiceException("每条线里需要几个该等级不能小于等于0");
			}
		}
		req.setUpdateTime(new Date());
		updateById(req);
		if(req.getLevel() == 1){
			//修改个人业绩
			lambdaUpdate()
				.gt(UserLevelConfig::getLevel,0)
				.set(UserLevelConfig::getPerformance,req.getPerformance())
				.update();
		}
		if(req.getLevel() == 2){
			//如果是区代理更新、小区、团队业绩考核条件、县代理、市代理、省代理、全国代理的业绩考核条件同样更新
			lambdaUpdate()
				.gt(UserLevelConfig::getLevel,1)
				.set(UserLevelConfig::getTeamPerformance,req.getTeamPerformance())
				.set(UserLevelConfig::getCommunityPerformance,req.getCommunityPerformance())
				.update();
		}
		return 1;
	}
}
