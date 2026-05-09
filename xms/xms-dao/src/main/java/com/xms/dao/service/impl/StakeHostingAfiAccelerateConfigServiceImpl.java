package com.xms.dao.service.impl;

import com.xms.dao.domain.StakeHostingAfiAccelerateConfig;
import com.xms.dao.mapper.StakeHostingAfiAccelerateConfigMapper;
import com.xms.dao.service.IStakeHostingAfiAccelerateConfigService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * AFI质押加速配置Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingAfiAccelerateConfigServiceImpl extends XmsDataServiceImpl<StakeHostingAfiAccelerateConfigMapper, StakeHostingAfiAccelerateConfig> implements IStakeHostingAfiAccelerateConfigService {
	public static final int STATUS_DISABLED = 0;
	public static final int STATUS_ENABLED = 1;

	@Override
	public List<StakeHostingAfiAccelerateConfig> selectStakeHostingAfiAccelerateConfigList(StakeHostingAfiAccelerateConfig config) {
		return baseMapper.selectStakeHostingAfiAccelerateConfigList(config);
	}

	@Override
	public StakeHostingAfiAccelerateConfig hitConfig(BigDecimal pledgeRatio) {
		if (pledgeRatio == null) {
			return null;
		}
		return lambdaQuery()
			.eq(StakeHostingAfiAccelerateConfig::getStatus, STATUS_ENABLED)
			.le(StakeHostingAfiAccelerateConfig::getPledgeRatio, pledgeRatio)
			.orderByDesc(StakeHostingAfiAccelerateConfig::getPledgeRatio)
			.orderByAsc(StakeHostingAfiAccelerateConfig::getSort)
			.last("limit 1")
			.one();
	}
}
