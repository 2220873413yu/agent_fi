package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.UserLevelConfig;

/**
 * 用户等级考核配置Service接口
 *
 * @author xms
 * @date 2025-12-03
 */
public interface IUserLevelConfigService extends XmsDataService<UserLevelConfig>
{

    /**
     * 查询用户等级考核配置列表
     *
     * @param userLevelConfig 用户等级考核配置
     * @return 用户等级考核配置集合
     */
    public List<UserLevelConfig> selectUserLevelConfigList(UserLevelConfig userLevelConfig);

	/**
	 * 更新用户配置信息
	 * @param userLevelConfig
	 * @return
	 */
	int updateRecordById(UserLevelConfig userLevelConfig);
}
