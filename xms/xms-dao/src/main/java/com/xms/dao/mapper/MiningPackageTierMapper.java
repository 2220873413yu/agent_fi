package com.xms.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

import com.xms.dao.domain.MiningPackageTier;

/**
 * 矿机质押区间配置Mapper接口
 *
 * @author xms
 * @date 2026-02-23
 */
public interface MiningPackageTierMapper extends XmsMapper<MiningPackageTier>
{
    /**
     * 查询矿机质押区间配置列表
     *
     * @param miningPackageTier 矿机质押区间配置
     * @return 矿机质押区间配置集合
     */
    public List<MiningPackageTier> selectMiningPackageTierList(MiningPackageTier miningPackageTier);

	/**
	 * 删除记录
	 * @param list
	 * @return
	 */
	int deleteRecordById(List<Long> list);

	/**
	 * 检测区间是否重叠
	 * @param startIndex
	 * @param endIndex
	 * @param excludeId
	 * @return
	 */
	int countOverlap(@Param("startIndex") Integer startIndex,
					 @Param("endIndex") Integer endIndex,
					 @Param("excludeId") Long excludeId);
}
