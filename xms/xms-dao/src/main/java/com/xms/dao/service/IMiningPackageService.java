package com.xms.dao.service;

import java.util.List;

import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.req.AdminAllocateMiningMachineRequest;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.MiningPackage;

/**
 * 矿机套餐Service接口
 *
 * @author xms
 * @date 2026-02-21
 */
public interface IMiningPackageService extends XmsDataService<MiningPackage>
{

    /**
     * 查询矿机套餐列表
     *
     * @param miningPackage 矿机套餐
     * @return 矿机套餐集合
     */
    public List<MiningPackage> selectMiningPackageList(MiningPackage miningPackage);

	/**
     * 修改矿机套餐
     *
     * @param miningPackage 矿机套餐
     * @return 结果
     */
    int updateMiningPackageById(MiningPackage miningPackage);


}
