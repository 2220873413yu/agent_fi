package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingAfiAccelerateConfig;

import java.math.BigDecimal;
import java.util.List;

/**
 * AFI质押加速配置Service接口
 *
 * @author xms
 */
public interface IStakeHostingAfiAccelerateConfigService extends XmsDataService<StakeHostingAfiAccelerateConfig> {
	List<StakeHostingAfiAccelerateConfig> selectStakeHostingAfiAccelerateConfigList(StakeHostingAfiAccelerateConfig config);

	StakeHostingAfiAccelerateConfig hitConfig(BigDecimal pledgeRatio);
}
