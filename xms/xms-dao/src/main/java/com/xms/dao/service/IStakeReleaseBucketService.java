package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.StakeReleaseBucket;

/**
 * 质押收益线性释放Service接口
 *
 * @author xms
 * @date 2026-02-27
 */
public interface IStakeReleaseBucketService extends XmsDataService<StakeReleaseBucket>
{

    /**
     * 查询质押收益线性释放列表
     *
     * @param stakeReleaseBucket 质押收益线性释放
     * @return 质押收益线性释放集合
     */
    public List<StakeReleaseBucket> selectStakeReleaseBucketList(StakeReleaseBucket stakeReleaseBucket);

}
