package com.xms.dao.service.impl;

import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.StakeHostingPackage;
import com.xms.dao.mapper.StakeHostingPackageMapper;
import com.xms.dao.service.IStakeHostingPackageService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * 托管套餐Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingPackageServiceImpl extends XmsDataServiceImpl<StakeHostingPackageMapper, StakeHostingPackage> implements IStakeHostingPackageService {
	private static final List<Integer> FIXED_DAYS = Arrays.asList(1, 30, 90, 180, 360);

	@Override
	public List<StakeHostingPackage> selectStakeHostingPackageList(StakeHostingPackage stakeHostingPackage) {
		return baseMapper.selectStakeHostingPackageList(stakeHostingPackage);
	}

	@Override
	public boolean save(StakeHostingPackage entity) {
		validatePackage(entity);
		return super.save(entity);
	}

	@Override
	public boolean updateById(StakeHostingPackage entity) {
		validatePackage(entity);
		return super.updateById(entity);
	}

	private void validatePackage(StakeHostingPackage entity) {
		if (entity == null) {
			throw new ServiceException("托管套餐不能为空");
		}
		if (!FIXED_DAYS.contains(entity.getDays())) {
			throw new ServiceException("托管天数只能为1/30/90/180/360");
		}
		if (entity.getMinAmount() == null || entity.getMinAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("起购金额必须大于0");
		}
		if (entity.getServiceFeeRatio() == null) {
			entity.setServiceFeeRatio(BigDecimal.ZERO);
		}
		if (entity.getServiceFeeRatio().compareTo(BigDecimal.ZERO) < 0) {
			throw new ServiceException("服务费比例不能小于0");
		}
		if (entity.getStatus() == null) {
			entity.setStatus(0);
		}
		if (entity.getSort() == null) {
			entity.setSort(entity.getDays());
		}
	}
}
