package com.xms.dao.service.impl;

import java.util.List;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.RewardStatDayMapper;
import com.xms.dao.domain.RewardStatDay;
import com.xms.dao.service.IRewardStatDayService;

/**
 * 每日奖励汇总Service业务层处理
 *
 * @author xms
 * @date 2025-11-23
 */
@Service
public class RewardStatDayServiceImpl extends XmsDataServiceImpl<RewardStatDayMapper, RewardStatDay> implements IRewardStatDayService
{


    /**
     * 查询每日奖励汇总列表
     *
     *
     * @param rewardStatDay 每日奖励汇总
     * @return 每日奖励汇总
     */
    @Override
    public List<RewardStatDay> selectRewardStatDayList(RewardStatDay rewardStatDay)
    {
        return baseMapper.selectRewardStatDayList(rewardStatDay);
    }

}
