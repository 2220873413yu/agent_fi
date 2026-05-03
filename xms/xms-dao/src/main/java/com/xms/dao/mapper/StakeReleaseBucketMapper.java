package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.StakeReleaseBucket;

/**
 * 质押收益线性释放Mapper接口
 *
 * @author xms
 * @date 2026-02-27
 */
public interface StakeReleaseBucketMapper extends XmsMapper<StakeReleaseBucket>
{
    /**
     * 查询质押收益线性释放列表
     *
     * @param stakeReleaseBucket 质押收益线性释放
     * @return 质押收益线性释放集合
     */
    public List<StakeReleaseBucket> selectStakeReleaseBucketList(StakeReleaseBucket stakeReleaseBucket);

}
