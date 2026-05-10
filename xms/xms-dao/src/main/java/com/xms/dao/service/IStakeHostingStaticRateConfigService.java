package com.xms.dao.service;

import com.xms.dao.domain.StakeHostingStaticRateConfig;

import java.math.BigDecimal;
import java.util.List;

/**
 * 托管G7静态收益率区间配置Service接口
 *
 * @author xms
 */
public interface IStakeHostingStaticRateConfigService extends XmsDataService<StakeHostingStaticRateConfig> {
	/**
	 * 查询托管G7静态收益率区间配置列表。
	 *
	 * @param config 查询条件
	 * @return 收益率区间配置集合
	 */
	List<StakeHostingStaticRateConfig> selectStakeHostingStaticRateConfigList(StakeHostingStaticRateConfig config);

	/**
	 * 根据Gsmooth匹配基础静态收益率。
	 *
	 * @param gSmooth 最近最多7天滚动平均增长率，单位%
	 * @return 基础静态收益率，单位%
	 */
	BigDecimal matchStaticRate(BigDecimal gSmooth);
}
