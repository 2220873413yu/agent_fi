package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingAfiAccelerateConfig;

import java.util.List;

/**
 * AFI质押加速配置Mapper接口
 *
 * @author xms
 */
public interface StakeHostingAfiAccelerateConfigMapper extends XmsMapper<StakeHostingAfiAccelerateConfig> {
	List<StakeHostingAfiAccelerateConfig> selectStakeHostingAfiAccelerateConfigList(StakeHostingAfiAccelerateConfig config);
}
