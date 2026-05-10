package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.StakeHostingStaticRateConfig;
import com.xms.dao.mapper.StakeHostingStaticRateConfigMapper;
import com.xms.dao.service.IStakeHostingStaticRateConfigService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管G7静态收益率区间配置Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingStaticRateConfigServiceImpl
	extends XmsDataServiceImpl<StakeHostingStaticRateConfigMapper, StakeHostingStaticRateConfig>
	implements IStakeHostingStaticRateConfigService {

	@Override
	public List<StakeHostingStaticRateConfig> selectStakeHostingStaticRateConfigList(StakeHostingStaticRateConfig config) {
		return lambdaQuery()
			.eq(config.getStatus() != null, StakeHostingStaticRateConfig::getStatus, config.getStatus())
			.ge(config.getMinG() != null, StakeHostingStaticRateConfig::getMinG, config.getMinG())
			.le(config.getMaxG() != null, StakeHostingStaticRateConfig::getMaxG, config.getMaxG())
			.eq(StakeHostingStaticRateConfig::getDeleted, 0)
			.orderByAsc(StakeHostingStaticRateConfig::getSort)
			.list();
	}

	@Override
	public BigDecimal matchStaticRate(BigDecimal gSmooth) {
		if (gSmooth == null) {
			gSmooth = BigDecimal.ZERO;
		}
		List<StakeHostingStaticRateConfig> configs = lambdaQuery()
			.eq(StakeHostingStaticRateConfig::getStatus, 1)
			.eq(StakeHostingStaticRateConfig::getDeleted, 0)
			.orderByAsc(StakeHostingStaticRateConfig::getSort)
			.list();
		if (CollectionUtil.isEmpty(configs)) {
			throw new ServiceException("托管G7静态收益率区间未配置");
		}
		for (StakeHostingStaticRateConfig config : configs) {
			BigDecimal minG = config.getMinG() == null ? new BigDecimal("-999999") : config.getMinG();
			BigDecimal maxG = config.getMaxG();
			boolean geMin = gSmooth.compareTo(minG) >= 0;
			boolean ltMax = maxG == null || gSmooth.compareTo(maxG) < 0;
			if (geMin && ltMax) {
				return config.getStaticRate() == null ? BigDecimal.ZERO : config.getStaticRate();
			}
		}
		throw new ServiceException("托管G7静态收益率区间未命中，Gsmooth=" + gSmooth);
	}
}
