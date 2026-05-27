package com.xms.dao.service.impl;

import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.DemoPledgePackage;
import com.xms.dao.mapper.DemoPledgePackageMapper;
import com.xms.dao.service.IDemoPledgePackageService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 示例质押套餐Service业务层处理。
 */
@Service
public class DemoPledgePackageServiceImpl extends XmsDataServiceImpl<DemoPledgePackageMapper, DemoPledgePackage> implements IDemoPledgePackageService {
	@Override
	public List<DemoPledgePackage> selectDemoPledgePackageList(DemoPledgePackage demoPledgePackage) {
		return baseMapper.selectDemoPledgePackageList(demoPledgePackage);
	}

	/**
	 * 保存示例套餐前校验金额、状态和排序默认值。
	 *
	 * @param entity 示例质押套餐
	 * @return 是否保存成功
	 */
	@Override
	public boolean save(DemoPledgePackage entity) {
		validatePackage(entity);
		return super.save(entity);
	}

	/**
	 * 修改示例套餐前校验金额、状态和排序默认值。
	 *
	 * @param entity 示例质押套餐
	 * @return 是否修改成功
	 */
	@Override
	public boolean updateById(DemoPledgePackage entity) {
		validatePackage(entity);
		return super.updateById(entity);
	}

	/**
	 * 校验示例质押套餐配置。
	 *
	 * @param entity 示例质押套餐
	 */
	private void validatePackage(DemoPledgePackage entity) {
		if (entity == null) {
			throw new ServiceException("示例质押套餐不能为空");
		}
		if (entity.getPackageName() == null || entity.getPackageName().trim().isEmpty()) {
			throw new ServiceException("套餐名称不能为空");
		}
		if (entity.getPledgeUsdtAmount() == null || entity.getPledgeUsdtAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("质押USDT金额必须大于0");
		}
		if (entity.getReleaseDays() == null || entity.getReleaseDays() <= 0) {
			throw new ServiceException("释放天数必须大于0");
		}
		if (entity.getDailyRate() == null || entity.getDailyRate().compareTo(BigDecimal.ZERO) < 0) {
			throw new ServiceException("日利率不能小于0");
		}
		if (entity.getStatus() == null) {
			entity.setStatus(1);
		}
		if (entity.getSort() == null) {
			entity.setSort(0);
		}
	}
}
