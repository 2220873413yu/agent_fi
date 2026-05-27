package com.xms.dao.service;

import com.xms.dao.domain.DemoPledgeLevelConfig;

import java.util.List;

/**
 * 示例质押等级配置Service接口。
 */
public interface IDemoPledgeLevelConfigService extends XmsDataService<DemoPledgeLevelConfig> {
	/**
	 * 查询示例质押等级配置列表。
	 *
	 * @param demoPledgeLevelConfig 查询条件
	 * @return 示例质押等级配置集合
	 */
	List<DemoPledgeLevelConfig> selectDemoPledgeLevelConfigList(DemoPledgeLevelConfig demoPledgeLevelConfig);
}
