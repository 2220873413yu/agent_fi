package com.xms.app.service;

import com.github.pagehelper.PageInfo;
import com.xms.app.entity.bo.MyAssetsBo;
import com.xms.app.entity.dto.RewardRecordDto;
import com.xms.app.entity.dto.TodayIncomeDto;
import com.xms.app.entity.resp.IncomeOverviewResp;
import com.xms.app.entity.vo.UserMoneySwapVo;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.dao.entity.bo.RewardRecordBo;
import com.xms.dao.entity.bo.UserMoneyBo;

import java.util.List;

public interface BizUserMoneyService {

	/**
	 * 查询用户钱包
	 * @param userId
	 * @return
	 */
	UserMoneyBo getUserMoney(Long userId);


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

	public List<RewardRecordDto> rewardRecord(Long lastId, List<Integer> sourceTypes);

	/**
	 * 查询我的资产
	 * @return
	 */
	MyAssetsBo myAssets();
}
