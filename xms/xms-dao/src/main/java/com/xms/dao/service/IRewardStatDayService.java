package com.xms.dao.service;

import java.util.List;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.RewardStatDay;

/**
 * 每日奖励汇总Service接口
 *
 * @author xms
 * @date 2025-11-23
 */
public interface IRewardStatDayService extends XmsDataService<RewardStatDay>
{

    /**
     * 查询每日奖励汇总列表
     *
     * @param rewardStatDay 每日奖励汇总
     * @return 每日奖励汇总集合
     */
    public List<RewardStatDay> selectRewardStatDayList(RewardStatDay rewardStatDay);

}
