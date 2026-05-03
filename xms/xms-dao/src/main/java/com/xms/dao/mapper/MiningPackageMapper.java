package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.MiningPackage;

/**
 * 基金套餐Mapper接口
 *
 * @author xms
 * @date 2025-08-07
 */
public interface MiningPackageMapper extends XmsMapper<MiningPackage>
{
    /**
     * 查询基金套餐列表
     *
     * @param miningPackage 基金套餐
     * @return 基金套餐集合
     */
    public List<MiningPackage> selectMiningPackageList(MiningPackage miningPackage);

}
