package com.xms.dao.service.impl;

import java.util.Date;
import java.util.List;

import cn.hutool.core.date.DateUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xms.dao.domain.RewardRecord;
import com.xms.dao.entity.bo.RewardRecordBo;
import com.xms.dao.mapper.RewardRecordMapper;
import com.xms.dao.service.IRewardRecordService;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 奖金记录Service业务层处理
 *
 * @author xms
 * @date 2025-11-19
 */
@Service
public class RewardRecordServiceImpl extends XmsDataServiceImpl<RewardRecordMapper, RewardRecord> implements IRewardRecordService
{


    /**
     * 查询奖金记录列表
     *
     *
     * @param rewardRecord 奖金记录
     * @return 奖金记录
     */
    @Override
    public List<RewardRecord> selectRewardRecordList(RewardRecord rewardRecord)
    {
        return baseMapper.selectRewardRecordList(rewardRecord);
    }

	/**
	 * 奖金记录(收益记录)
	 * @param pageIndex 当前页 默认1
	 * @param pageSize 每页长度 默认20(最大20)
	 * @param bizType 业务类型 -1:查询全部,1:静态收益,2:动态收益,3:团队奖励
	 * @param dateType 时间类型-1:查询全部,1:今日,2:本周,3:本月
	 * @return
	 */
	@Override
	public PageInfo<RewardRecordBo> rewardList(Integer pageIndex, Integer pageSize, Long userId, Integer bizType,
											   Integer dateType) {
		PageHelper.startPage(pageIndex, pageSize);
		Date startTime = null;
		Date endTime = null;
		if (dateType != null && dateType > 0) {
			Date now = new Date();
			if (dateType == 1) {
				startTime = DateUtil.beginOfDay(now);
				endTime = DateUtil.endOfDay(now);
			} else if (dateType == 2) {
				startTime = DateUtil.beginOfWeek(now);
				endTime = DateUtil.endOfWeek(now);
			} else if (dateType == 3) {
				startTime = DateUtil.beginOfMonth(now);
				endTime = DateUtil.endOfMonth(now);
			}
		}
		PageInfo<RewardRecordBo> pageInfo = new PageInfo<RewardRecordBo>(this.baseMapper.rewardList(userId, bizType, startTime, endTime));
		return pageInfo;
	}


}
