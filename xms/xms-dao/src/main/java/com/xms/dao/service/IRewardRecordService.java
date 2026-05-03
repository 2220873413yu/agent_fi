package com.xms.dao.service;

import java.util.List;

import com.github.pagehelper.PageInfo;
import com.xms.dao.entity.bo.RewardRecordBo;
import com.xms.dao.entity.bo.UserMoneyLogBo;
import com.xms.dao.service.XmsDataService;
import com.xms.dao.domain.RewardRecord;

/**
 * 奖金记录Service接口
 *
 * @author xms
 * @date 2025-11-19
 */
public interface IRewardRecordService extends XmsDataService<RewardRecord>
{

    /**
     * 查询奖金记录列表
     *
     * @param rewardRecord 奖金记录
     * @return 奖金记录集合
     */
    public List<RewardRecord> selectRewardRecordList(RewardRecord rewardRecord);

	/**
	 * 奖金记录(收益记录)
	 * @param pageIndex 当前页 默认1
	 * @param pageSize 每页长度 默认20(最大20)
	 * @param bizType 业务类型 -1:查询全部,1:静态收益,2:动态收益,3:团队奖励
	 * @param dateType 时间类型-1:查询全部,1:今日,2:本周,3:本月
	 * @return
	 */
    PageInfo<RewardRecordBo> rewardList(Integer pageIndex, Integer pageSize, Long userId, Integer bizType,
										Integer dateType);
}
