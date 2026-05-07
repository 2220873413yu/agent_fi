package com.xms.dao.mapper;

import com.xms.dao.domain.StakeHostingPackage;

import java.util.List;

/**
 * 托管套餐Mapper接口
 *
 * @author xms
 */
public interface StakeHostingPackageMapper extends XmsMapper<StakeHostingPackage> {
	/**
	 * 查询托管套餐列表
	 *
	 * @param stakeHostingPackage 托管套餐
	 * @return 托管套餐集合
	 */
	List<StakeHostingPackage> selectStakeHostingPackageList(StakeHostingPackage stakeHostingPackage);
}
