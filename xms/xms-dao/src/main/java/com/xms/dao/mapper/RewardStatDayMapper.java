package com.xms.dao.mapper;

import java.util.List;
import com.xms.dao.mapper.XmsMapper;

import com.xms.dao.domain.RewardStatDay;

/**
 * 每日奖励汇总Mapper接口
 *
 * @author xms
 * @date 2025-11-23
 */
public interface RewardStatDayMapper extends XmsMapper<RewardStatDay>
{
    /**
     * 查询每日奖励汇总列表
     *
     * @param rewardStatDay 每日奖励汇总
     * @return 每日奖励汇总集合
     */
    public List<RewardStatDay> selectRewardStatDayList(RewardStatDay rewardStatDay);

}
