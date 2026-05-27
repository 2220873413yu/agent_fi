package com.xms.dao.service.impl;

import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.DemoPledgeLevelConfig;
import com.xms.dao.mapper.DemoPledgeLevelConfigMapper;
import com.xms.dao.service.IDemoPledgeLevelConfigService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 示例质押等级配置Service业务层处理。
 */
@Service
public class DemoPledgeLevelConfigServiceImpl
	extends XmsDataServiceImpl<DemoPledgeLevelConfigMapper, DemoPledgeLevelConfig>
	implements IDemoPledgeLevelConfigService {

	@Override
	public List<DemoPledgeLevelConfig> selectDemoPledgeLevelConfigList(DemoPledgeLevelConfig demoPledgeLevelConfig) {
		return baseMapper.selectDemoPledgeLevelConfigList(demoPledgeLevelConfig);
	}

	/**
	 * 保存示例等级配置前校验等级和三项业绩门槛。
	 *
	 * @param entity 示例质押等级配置
	 * @return 是否保存成功
	 */
	@Override
	public boolean save(DemoPledgeLevelConfig entity) {
		validateConfig(entity);
		return super.save(entity);
	}

	/**
	 * 修改示例等级配置前校验等级和三项业绩门槛。
	 *
	 * @param entity 示例质押等级配置
	 * @return 是否修改成功
	 */
	@Override
	public boolean updateById(DemoPledgeLevelConfig entity) {
		validateConfig(entity);
		return super.updateById(entity);
	}

	/**
	 * 校验等级配置基础规则。
	 *
	 * <p>示例等级规则允许0门槛，表示该项不限制；金额字段不允许负数。</p>
	 *
	 * @param entity 示例质押等级配置
	 */
	private void validateConfig(DemoPledgeLevelConfig entity) {
		if (entity == null) {
			throw new ServiceException("示例质押等级配置不能为空");
		}
		if (entity.getLevel() == null || entity.getLevel() < 0) {
			throw new ServiceException("等级编码不能小于0");
		}
		if (isNegative(entity.getPerformance())) {
			throw new ServiceException("个人业绩门槛不能小于0");
		}
		if (isNegative(entity.getTeamPerformance())) {
			throw new ServiceException("团队业绩门槛不能小于0");
		}
		if (isNegative(entity.getCommunityPerformance())) {
			throw new ServiceException("小区业绩门槛不能小于0");
		}
		if (entity.getPerformance() == null) {
			entity.setPerformance(BigDecimal.ZERO);
		}
		if (entity.getTeamPerformance() == null) {
			entity.setTeamPerformance(BigDecimal.ZERO);
		}
		if (entity.getCommunityPerformance() == null) {
			entity.setCommunityPerformance(BigDecimal.ZERO);
		}
	}

	/**
	 * 判断金额是否为负数。
	 *
	 * @param amount 金额
	 * @return true 表示金额小于0
	 */
	private boolean isNegative(BigDecimal amount) {
		return amount != null && amount.compareTo(BigDecimal.ZERO) < 0;
	}
}
