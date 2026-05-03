package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.UserLevelConfig;

/**
 * 用户等级考核配置Mapper接口
 *
 * @author xms
 * @date 2025-12-03
 */
public interface UserLevelConfigMapper extends XmsMapper<UserLevelConfig>
{
    /**
     * 查询用户等级考核配置列表
     *
     * @param userLevelConfig 用户等级考核配置
     * @return 用户等级考核配置集合
     */
    public List<UserLevelConfig> selectUserLevelConfigList(UserLevelConfig userLevelConfig);

}
