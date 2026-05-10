package com.xms.app.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageInfo;
import com.xms.app.entity.bo.MyAssetsBo;
import com.xms.app.entity.dto.RewardRecordDto;
import com.xms.app.entity.dto.TodayIncomeDto;
import com.xms.app.entity.resp.IncomeOverviewResp;
import com.xms.app.entity.vo.UserMoneySwapVo;
import com.xms.app.service.BizUserMoneyService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.*;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.CollectionUtil;
import com.xms.common.utils.Func;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.RewardRecord;
import com.xms.dao.domain.RewardStatDay;
import com.xms.dao.domain.StakeOrder;
import com.xms.dao.entity.bo.RewardRecordBo;
import com.xms.dao.entity.bo.UserMoneyBo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.domain.UserMoneyLog;
import com.xms.dao.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BizUserMoneyServiceImpl implements BizUserMoneyService {

	@Autowired
	private IUserMoneyService userMoneyService;
	@Autowired
	private IRewardRecordService rewardRecordService;

	@Autowired
	private IRewardStatDayService rewardStatDayService;

	@Autowired
	private IStakeOrderService stakeOrderService;

	@Override
	public MyAssetsBo myAssets() {
		MyAssetsBo bo = new MyAssetsBo();
		Long userId = SecurityUtils.getLoginAppUser().getUserId();
		BigDecimal activeTotalReward= stakeOrderService.lambdaQuery()
			.eq(StakeOrder::getUserId, userId)
			.eq(StakeOrder::getStatus,1)
			.select(StakeOrder::getRemainingOutAmount)
			.list().stream()
			.map(StakeOrder::getRemainingOutAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
		bo.setActiveTotalReward(activeTotalReward);

		List<RewardRecord> rewardRecordList = rewardRecordService.lambdaQuery()
			.eq(RewardRecord::getUserId,userId)
			.in(RewardRecord::getSourceType,1,2,3,4,5,6)
			.select(RewardRecord::getAmount,RewardRecord::getSourceType)
			.apply("DATE(create_time) = CURDATE()")
			.list().stream().collect(Collectors.toList());
			//除了sourceType6之外都是动态计算一下，static计算一下
			BigDecimal staticTotalReward = BigDecimal.ZERO;
			BigDecimal dynamicTotalReward = BigDecimal.ZERO;
			for (RewardRecord record : rewardRecordList) {
				if(record.getSourceType() != 6){
					dynamicTotalReward = dynamicTotalReward.add(record.getAmount());
				}else{
					staticTotalReward = staticTotalReward.add(record.getAmount());
				}
			}
			bo.setTodayDynamicTotalReward(dynamicTotalReward);
			bo.setTodayStaticTotalReward(staticTotalReward);
		return bo;
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
		return rewardRecordService.rewardList(pageIndex,pageSize,userId,bizType,dateType);
	}


	private RewardSum filterSum(List<RewardStatDay> statList, Long start, Long end) {
		if (CollectionUtil.isEmpty(statList)) {
			return new RewardSum(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		}
		BigDecimal staticValue = BigDecimal.ZERO;
		BigDecimal dynamicValue = BigDecimal.ZERO;
		BigDecimal teamValue = BigDecimal.ZERO;
		for (RewardStatDay statDay : statList) {
			Long statDate = statDay.getStatDate();
			boolean ge = start == null || statDate >= start;
			boolean le = end == null || statDate <= end;
			if (ge && le) {
				staticValue = staticValue.add(defaultValue(statDay.getStaticAmount()));
				dynamicValue = dynamicValue.add(defaultValue(statDay.getDynamicAmount()));
				teamValue = teamValue.add(defaultValue(statDay.getTeamAmount()));
			}
		}
		return new RewardSum(staticValue, dynamicValue, teamValue);
	}

	private RewardSum aggregateAll(Long userId) {
		RewardStatDay aggregate = rewardStatDayService.getBaseMapper().selectOne(
			new QueryWrapper<RewardStatDay>()
				.select("COALESCE(SUM(static_amount),0) AS static_amount",
					"COALESCE(SUM(dynamic_amount),0) AS dynamic_amount",
					"COALESCE(SUM(team_amount),0) AS team_amount")
				.eq("user_id", userId)
		);
		if (aggregate == null) {
			return new RewardSum(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		}
		return new RewardSum(aggregate.getStaticAmount(), aggregate.getDynamicAmount(), aggregate.getTeamAmount());
	}

	private IncomeOverviewResp.IncomeScopeDetail buildScopeDetail(Integer scopeType, RewardSum sum) {
		BigDecimal totalAmount = sum.staticAmount
			.add(sum.dynamicAmount)
			.add(sum.teamAmount);

		IncomeOverviewResp.IncomeScopeDetail detail = new IncomeOverviewResp.IncomeScopeDetail();
		detail.setScopeType(scopeType);
		detail.setAmount(totalAmount);
		List<IncomeOverviewResp.IncomeTypePortion> portions = new ArrayList<>();
		portions.add(buildPortion(1, sum.staticAmount, totalAmount));
		portions.add(buildPortion(2, sum.dynamicAmount, totalAmount));
		portions.add(buildPortion(3, sum.teamAmount, totalAmount));
		detail.setTypePortions(portions);
		return detail;
	}

	private IncomeOverviewResp.IncomeTypePortion buildPortion(Integer type, BigDecimal amount, BigDecimal total) {
		IncomeOverviewResp.IncomeTypePortion portion = new IncomeOverviewResp.IncomeTypePortion();
		portion.setType(type);
		BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;
		portion.setAmount(safeAmount);
		if (total == null || total.compareTo(BigDecimal.ZERO) == 0) {
			portion.setRatio(BigDecimal.ZERO);
		} else {
			portion.setRatio(safeAmount
				.divide(total, ConstantStatic.twoScale, ConstantStatic.roundingModeNew)
				.multiply(new BigDecimal("100")));
		}
		return portion;
	}

	private BigDecimal defaultValue(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount;
	}

	/**
	 * 获取奖金记录
	 * @param lastId
	 * @param sourceTypes
	 * @return
	 */
	@Override
	public List<RewardRecordDto> rewardRecord(Long lastId, List<Integer> sourceTypes) {
		if(CollectionUtil.isEmpty(sourceTypes)){
			return new ArrayList<>();
		}
		List<RewardRecordDto> result = rewardRecordService.lambdaQuery()
			.eq(RewardRecord::getUserId, SecurityUtils.getFrontUserId())
			.in(RewardRecord::getSourceType, sourceTypes)
			.lt(Func.isNotEmpty(lastId), RewardRecord::getId, lastId)
			.select(RewardRecord::getId, RewardRecord::getAmount, RewardRecord::getSourceType,
				RewardRecord::getSourceOrderCode, RewardRecord::getSourceUserId,
				RewardRecord::getCreateTime, RewardRecord::getRealTimePrice)
			.orderByDesc(RewardRecord::getId).last(SysConstant.PAGE_LIMIT)
			.list().stream().map(item -> {
				RewardRecordDto rewardRecordDto = new RewardRecordDto();
				rewardRecordDto.setId(item.getId());
				rewardRecordDto.setAmount(item.getAmount());
				rewardRecordDto.setSourceType(item.getSourceType());
				rewardRecordDto.setSourceOrderCode(item.getSourceOrderCode());
				rewardRecordDto.setSourceUserId(item.getSourceUserId());
				rewardRecordDto.setCreateTime(item.getCreateTime());
				rewardRecordDto.setRealTimePrice(item.getRealTimePrice());
				return rewardRecordDto;
			}).collect(Collectors.toList());
		return result;
	}

	/**
	 * 查询用户钱包
	 *
	 * @param userId
	 * @return
	 */
	@Override
	public UserMoneyBo getUserMoney(Long userId) {
		//查询钱包
		UserMoney userMoney = userMoneyService.lambdaQuery().eq(UserMoney::getId, userId)
			.select(UserMoney::getValidNum1, UserMoney::getValidNum2)
			.one();
		UserMoneyBo userMoneyBo = new UserMoneyBo();
		userMoneyBo.setValidNum1(userMoney.getValidNum1());
		userMoneyBo.setValidNum2(userMoney.getValidNum2());
		return userMoneyBo;
	}


	private static class RewardSum {
		private final BigDecimal staticAmount;
		private final BigDecimal dynamicAmount;
		private final BigDecimal teamAmount;

		private RewardSum(BigDecimal staticAmount, BigDecimal dynamicAmount, BigDecimal teamAmount) {
			this.staticAmount = staticAmount == null ? BigDecimal.ZERO : staticAmount;
			this.dynamicAmount = dynamicAmount == null ? BigDecimal.ZERO : dynamicAmount;
			this.teamAmount = teamAmount == null ? BigDecimal.ZERO : teamAmount;
		}
	}
}
