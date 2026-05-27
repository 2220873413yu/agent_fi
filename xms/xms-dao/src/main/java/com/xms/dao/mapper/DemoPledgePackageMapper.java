package com.xms.dao.mapper;

import com.xms.dao.domain.DemoPledgePackage;

import java.util.List;

/**
 * 示例质押套餐Mapper接口。
 */
public interface DemoPledgePackageMapper extends XmsMapper<DemoPledgePackage> {
	/**
	 * 查询示例质押套餐列表。
	 *
	 * @param demoPledgePackage 查询条件
	 * @return 示例质押套餐集合
	 */
	List<DemoPledgePackage> selectDemoPledgePackageList(DemoPledgePackage demoPledgePackage);
}
