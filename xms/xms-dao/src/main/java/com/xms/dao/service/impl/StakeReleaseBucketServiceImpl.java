package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.StakeReleaseBucketMapper;
import com.xms.dao.domain.StakeReleaseBucket;
import com.xms.dao.service.IStakeReleaseBucketService;

/**
 * 质押收益线性释放Service业务层处理
 *
 * @author xms
 * @date 2026-02-27
 */
@Service
public class StakeReleaseBucketServiceImpl extends XmsDataServiceImpl<StakeReleaseBucketMapper, StakeReleaseBucket> implements IStakeReleaseBucketService
{


    /**
     * 查询质押收益线性释放列表
     *
     *
     * @param stakeReleaseBucket 质押收益线性释放
     * @return 质押收益线性释放
     */
    @Override
    public List<StakeReleaseBucket> selectStakeReleaseBucketList(StakeReleaseBucket stakeReleaseBucket)
    {
        return baseMapper.selectStakeReleaseBucketList(stakeReleaseBucket);
    }

}
