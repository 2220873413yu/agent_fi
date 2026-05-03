package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.MiningPackageTier;

/**
 * 矿机质押区间配置Service接口
 *
 * @author xms
 * @date 2026-02-23
 */
public interface IMiningPackageTierService extends XmsDataService<MiningPackageTier>
{

    /**
     * 查询矿机质押区间配置列表
     *
     * @param miningPackageTier 矿机质押区间配置
     * @return 矿机质押区间配置集合
     */
    public List<MiningPackageTier> selectMiningPackageTierList(MiningPackageTier miningPackageTier);

	/**
	 * 新增矿机质押区间配置
	 * @param miningPackageTier
	 * @return
	 */
	int saveMiningPackageTier(MiningPackageTier miningPackageTier);

	/**
	 * 修改
	 * @param miningPackageTier
	 * @return
	 */
	int updateMiningPackageTier(MiningPackageTier miningPackageTier);

	/**
	 * 删除
	 * @param list
	 * @return
	 */
	int removeRecordById(List<Long> list);
}
