package com.xms.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantSys;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.SysConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.RewardRecord;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.domain.StakeHostingPackage;
import com.xms.dao.domain.StakeHostingRewardSettlement;
import com.xms.dao.domain.UserLevelConfig;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.vo.ParentUserTaskVo;
import com.xms.dao.service.IRewardRecordService;
import com.xms.dao.service.IStakeHostingPackageService;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IStakeHostingRewardSettlementService;
import com.xms.dao.service.ISysParaService;
import com.xms.dao.service.IUserLevelConfigService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import com.xms.dao.service.impl.StakeHostingOrderServiceImpl;
import com.xms.web.service.IAsyncTaskService;
import com.xms.web.service.IStakeHostingTaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 托管定时任务Service实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class StakeHostingTaskServiceImpl implements IStakeHostingTaskService {
	/**
	 * 第一批占位静态收益率：0.5%。
	 * 第二批接入G7时替换 calculatePlaceholderStaticRate 的内部实现。
	 */
	private static final BigDecimal PLACEHOLDER_STATIC_RATE = new BigDecimal("0.005");

	private final IStakeHostingOrderService stakeHostingOrderService;
	private final UserWalletService userWalletService;
	private final IRewardRecordService rewardRecordService;
	private final IAsyncTaskService asyncTaskServiceImpl;
	private final IStakeHostingPackageService stakeHostingPackageService;
	private final IStakeHostingRewardSettlementService stakeHostingRewardSettlementService;
	private final UserInfoService userInfoService;
	private final IUserLevelConfigService userLevelConfigService;
	private final ISysParaService sysParaServiceImpl;

	private static final int REWARD_TYPE_STATIC_FEE = 1;
	private static final int REWARD_TYPE_DIRECT = 2;
	private static final int REWARD_TYPE_DIFF = 3;
	private static final int REWARD_TYPE_SAME_LEVEL = 4;
	private static final int REWARD_TYPE_PLATFORM = 5;
	private static final int ARRIVAL_NO = 0;
	private static final int ARRIVAL_YES = 1;
	private static final int SKIP_NO_ACTIVE_ORDER = 2;
	private static final int SKIP_INVALID_USER = 4;

	/**
	 * 每日发放托管订单静态收益。
	 *
	 * <p>使用 101 任务记录控制每天只成功执行一次；当前自测阶段暂时不按订单 lastRewardDay 过滤，
	 * 方便手动重复验证，后续上线前需要恢复订单级日期过滤。</p>
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void distributeDailyStaticReward() {
		String strDate = DateUtil.format(DateUtil.date(), "yyyyMMdd");
		int rewardDay = Integer.parseInt(strDate);
		// 任务级幂等：同一天的 101 任务已经成功记录时，直接跳过。
		Map<String, Object> task = asyncTaskServiceImpl.getTask(SysConstant.TSK_TYPE_101, strDate);
		if (!CollectionUtil.isEmpty(task)) {
			log.debug("任务类型101 每天发放托管静态收益任务已存在跳过");
			return;
		}

		// 自测阶段扫描所有产出中的托管订单；上线前恢复 lastRewardDay 过滤，防止订单当天重复发放。
		List<StakeHostingOrder> orderList = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			// 自测阶段先不按 lastRewardDay 过滤，方便重复验证发放流程；上线前恢复订单级幂等过滤。
//			.and(wrapper -> wrapper.ne(StakeHostingOrder::getLastRewardDay, rewardDay).or().isNull(StakeHostingOrder::getLastRewardDay))
			.list();
		if (CollectionUtil.isEmpty(orderList)) {
			log.info("托管每日静态收益：无待发放订单");
			addDailyTask(strDate);
			return;
		}
		Date now = new Date();
		for (StakeHostingOrder order : orderList) {
			distributeOne(order, rewardDay, now);
		}
		// 所有订单处理完成后再写入任务记录，避免中途失败造成当天任务被误标记完成。
		addDailyTask(strDate);
	}

	/**
	 * 记录当天 101 托管静态收益任务已执行。
	 */
	private void addDailyTask(String strDate) {
		int rows = asyncTaskServiceImpl.addTask(SysConstant.TSK_TYPE_101, strDate);
		if (rows != 1) {
			throw new RuntimeException("任务类型101 每天发放托管静态收益任务插入失败");
		}
	}

	/**
	 * 给单笔产出中的托管订单发放一次静态收益。
	 *
	 * <p>主要步骤：</p>
	 * <p>1. 按当前静态收益率计算本次收益。</p>
	 * <p>2. 收益入账用户 USDT 钱包，并写入奖励记录。</p>
	 * <p>3. 累加订单已发收益和运行天数，更新最近发放日期。</p>
	 * <p>4. 判断是否回本、是否达到套餐天数。</p>
	 * <p>5. 订单发满后改为已完成，并扣减对应托管业绩。</p>
	 */
	private void distributeOne(StakeHostingOrder order, int rewardDay, Date now) {
		BigDecimal todayRate = calculatePlaceholderStaticRate(order);
		BigDecimal grossReward = order.getStakeUsdtAmount().multiply(todayRate)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal serviceFeeRatio = getServiceFeeRatio(order);
		BigDecimal serviceFee = grossReward.multiply(serviceFeeRatio)
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal reward = grossReward.subtract(serviceFee)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		int nextRunDays = (order.getRunDays() == null ? 0 : order.getRunDays()) + 1;
		BigDecimal totalReward = (order.getTotalStaticReward() == null ? BigDecimal.ZERO : order.getTotalStaticReward())
			.add(reward)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);

		saveSettlement(order, null, REWARD_TYPE_STATIC_FEE, null, grossReward, serviceFeeRatio,
			serviceFee, grossReward, serviceFeeRatio, serviceFee, reward, ARRIVAL_YES, null, rewardDay, now);
		if (reward.compareTo(BigDecimal.ZERO) > 0) {
			int rows = userWalletService.handerUserMoney(reward, order.getOrderNo(), order.getUserId(), order.getUserId(),
				ConstantType.user_money_log_source_type.type_31, ConstantType.user_money_coin_type.type_1);
			if (rows != 1) {
				throw new ServiceException("托管静态收益入账失败，userId=" + order.getUserId());
			}
			RewardRecord rewardRecord = new RewardRecord();
			rewardRecord.setOrderCode(IDUtils.getSnowflakeStr());
			rewardRecord.setUserId(order.getUserId());
			rewardRecord.setAmount(reward);
			rewardRecord.setCoinType(ConstantType.user_money_coin_type.type_1);
			rewardRecord.setSourceType(ConstantType.xms_reward_record_source_type.type_27);
			rewardRecord.setSourceOrderCode(order.getOrderNo());
			rewardRecord.setSourceUserId(order.getUserId());
			rewardRecord.setGtId(IDUtils.getSnowflakeStr());
			rewardRecord.setCreateTime(now);
			rewardRecordService.save(rewardRecord);
		}
		if (order.getSourceType() != null && order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_USER
			&& reward.compareTo(BigDecimal.ZERO) > 0) {
			distributeTeamReward(order, grossReward, serviceFeeRatio, serviceFee, reward, rewardDay, now);
		}

		StakeHostingOrder update = new StakeHostingOrder();
		update.setId(order.getId());
		update.setTodayReward(reward);
		update.setTotalStaticReward(totalReward);
		update.setRunDays(nextRunDays);
		update.setLastRewardDay(rewardDay);
		update.setIsReturnPrincipal(totalReward.compareTo(order.getStakeUsdtAmount()) >= 0 ? 1 : 0);
		update.setUpdateTime(now);
		boolean finished = nextRunDays >= order.getPackageDays();
		if (finished) {
			update.setStatus(StakeHostingOrderServiceImpl.STATUS_FINISHED);
			update.setFinishTime(now);
		}
		if (!stakeHostingOrderService.updateById(update)) {
			throw new ServiceException("更新托管订单收益失败，orderNo=" + order.getOrderNo());
		}
		if (finished) {
			stakeHostingOrderService.subtractHostingPerformance(order.getUserId(), order.getStakeUsdtAmount(), order.getId());
		}
	}

	private BigDecimal calculatePlaceholderStaticRate(StakeHostingOrder order) {
		return PLACEHOLDER_STATIC_RATE;
	}

	private BigDecimal getServiceFeeRatio(StakeHostingOrder order) {
		if (order.getPackageId() == null) {
			return BigDecimal.ZERO;
		}
		StakeHostingPackage hostingPackage = stakeHostingPackageService.getById(order.getPackageId());
		return hostingPackage == null || hostingPackage.getServiceFeeRatio() == null ? BigDecimal.ZERO : hostingPackage.getServiceFeeRatio();
	}

	private void distributeTeamReward(StakeHostingOrder order, BigDecimal grossReward, BigDecimal serviceFeeRatio,
									  BigDecimal serviceFee, BigDecimal netReward, int rewardDay, Date now) {
		List<ParentUserTaskVo> parentUsers = userInfoService.getParentUserTaskVo(order.getUserId());
		if (CollectionUtil.isEmpty(parentUsers)) {
			log.info("托管团队奖励跳过：源订单无上级，orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
			return;
		}
		distributeDirectReward(order, parentUsers.get(0), grossReward, serviceFeeRatio, serviceFee, netReward, rewardDay, now);
		distributeDiffAndSameLevelReward(order, parentUsers, grossReward, serviceFeeRatio, serviceFee, netReward, rewardDay, now);
	}

	private void distributeDirectReward(StakeHostingOrder order, ParentUserTaskVo directUser, BigDecimal grossReward,
										BigDecimal serviceFeeRatio, BigDecimal serviceFee, BigDecimal netReward,
										int rewardDay, Date now) {
		BigDecimal directRatioPercent = getDirectRewardRatioPercent();
		BigDecimal directReward = calculateReward(netReward, directRatioPercent);
		if (directUser == null) {
			log.info("托管直推奖励跳过：源订单无直属上级，orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
			return;
		}
		Integer skipReason = getRewardSkipReason(directUser);
		if (skipReason != null) {
			saveSettlement(order, directUser.getUserId(), REWARD_TYPE_PLATFORM, effectiveLevel(directUser), netReward, directRatioPercent,
				directReward, grossReward, serviceFeeRatio, serviceFee, netReward, ARRIVAL_NO, skipReason, rewardDay, now);
			return;
		}
		grantReward(order, directUser.getUserId(), REWARD_TYPE_DIRECT, effectiveLevel(directUser), netReward, directRatioPercent,
			directReward, grossReward, serviceFeeRatio, serviceFee, netReward, rewardDay, now);
	}

	private void distributeDiffAndSameLevelReward(StakeHostingOrder order, List<ParentUserTaskVo> parentUsers,
												  BigDecimal grossReward, BigDecimal serviceFeeRatio, BigDecimal serviceFee,
												  BigDecimal netReward, int rewardDay, Date now) {
		Map<Integer, BigDecimal> levelRatioMap = getLevelRatioMap();
		BigDecimal coveredRatio = BigDecimal.ZERO;
		for (int i = 0; i < parentUsers.size(); i++) {
			ParentUserTaskVo parent = parentUsers.get(i);
			Integer level = effectiveLevel(parent);
			BigDecimal levelRatio = levelRatioMap.getOrDefault(level, BigDecimal.ZERO);
			if (levelRatio.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			BigDecimal diffRatio = levelRatio.subtract(coveredRatio);
			if (diffRatio.compareTo(BigDecimal.ZERO) > 0) {
				int sameCount = level >= 5 ? countSameLevelRun(parentUsers, i, level) : 1;
				BigDecimal diffPool = calculateReward(netReward, diffRatio);
				boolean hasCoveredUser = false;
				for (int sameIndex = 0; sameIndex < sameCount; sameIndex++) {
					ParentUserTaskVo rewardUser = parentUsers.get(i + sameIndex);
					int rewardType = sameIndex == 0 ? REWARD_TYPE_DIFF : REWARD_TYPE_SAME_LEVEL;
					BigDecimal rewardAmount = sameCount == 1 ? diffPool : calculateSameLevelReward(diffPool, sameIndex + 1, sameCount);
					Integer skipReason = getRewardSkipReason(rewardUser);
					if (skipReason == null) {
						hasCoveredUser = true;
						grantReward(order, rewardUser.getUserId(), rewardType, level, netReward, diffRatio,
							rewardAmount, grossReward, serviceFeeRatio, serviceFee, netReward, rewardDay, now);
					} else {
						saveSettlement(order, rewardUser.getUserId(), REWARD_TYPE_PLATFORM, level, netReward, diffRatio,
							rewardAmount, grossReward, serviceFeeRatio, serviceFee, netReward, ARRIVAL_NO, skipReason, rewardDay, now);
					}
				}
				if (hasCoveredUser) {
					coveredRatio = levelRatio;
				}
				i += sameCount - 1;
			}
		}
	}

	private int countSameLevelRun(List<ParentUserTaskVo> parentUsers, int startIndex, Integer level) {
		int count = 0;
		for (int i = startIndex; i < parentUsers.size(); i++) {
			if (!level.equals(effectiveLevel(parentUsers.get(i)))) {
				break;
			}
			count++;
		}
		return count;
	}

	private BigDecimal calculateSameLevelReward(BigDecimal pool, int sameIndex, int sameCount) {
		if (sameCount <= 1 || pool.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		int power = sameIndex == sameCount ? sameCount - 1 : sameIndex;
		BigDecimal divisor = new BigDecimal(2).pow(power);
		return pool.divide(divisor, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	private Integer getRewardSkipReason(ParentUserTaskVo user) {
		if (user.getIsValid() == null || user.getIsValid() == 0) {
			return SKIP_INVALID_USER;
		}
		if (!hasUnfinishedHostingOrder(user.getUserId())) {
			return SKIP_NO_ACTIVE_ORDER;
		}
		return null;
	}

	/**
	 * 是否存在可作为团队奖励资格的未结束托管订单。
	 *
	 * <p>当前业务只会生成待支付、产出中、已完成三类状态，`STATUS_PAUSED` 是预留状态，
	 * 暂无暂停托管订单流程；因此这里按“支付成功或后台拨付，且未完成”判断有效资格。</p>
	 */
	private boolean hasUnfinishedHostingOrder(Long userId) {
		return stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getUserId, userId)
			.and(wrapper -> wrapper.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
				.or().eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_ADMIN))
			.ne(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_FINISHED)
			.count() > 0;
	}

	private Map<Integer, BigDecimal> getLevelRatioMap() {
		Map<Integer, BigDecimal> levelRatioMap = new HashMap<>();
		levelRatioMap.put(0, BigDecimal.ZERO);
		List<UserLevelConfig> configs = userLevelConfigService.lambdaQuery()
			.gt(UserLevelConfig::getLevel, 0)
			.list();
		if (CollectionUtil.isNotEmpty(configs)) {
			for (UserLevelConfig config : configs) {
				levelRatioMap.put(config.getLevel(), config.getTeamRewardRatio() == null ? BigDecimal.ZERO : config.getTeamRewardRatio());
			}
		}
		return levelRatioMap;
	}

	private BigDecimal getDirectRewardRatioPercent() {
		String value = sysParaServiceImpl.getValue(ConstantSys.biz_stake_hosting_direct_reward_ratio);
		if (StrUtil.isBlank(value)) {
			throw new ServiceException("托管直推奖励比例未配置");
		}
		return new BigDecimal(value);
	}

	private BigDecimal calculateReward(BigDecimal baseAmount, BigDecimal ratioPercent) {
		if (baseAmount == null || ratioPercent == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0 || ratioPercent.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		return baseAmount.multiply(ratioPercent)
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	private Integer effectiveLevel(ParentUserTaskVo user) {
		return Math.max(Math.max(defaultLevel(user.getGameLevel()), defaultLevel(user.getMinGameLevel())), defaultLevel(user.getAdminGameLevel()));
	}

	private int defaultLevel(Integer level) {
		return level == null ? 0 : level;
	}

	private void grantReward(StakeHostingOrder order, Long receiveUserId, int rewardType, Integer rewardLevel,
							 BigDecimal rewardBase, BigDecimal ratioPercent, BigDecimal rewardAmount,
							 BigDecimal grossReward, BigDecimal serviceFeeRatio, BigDecimal serviceFee,
							 BigDecimal netReward, int rewardDay, Date now) {
		if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		int moneySourceType = rewardType == REWARD_TYPE_DIRECT ? ConstantType.user_money_log_source_type.type_32
			: rewardType == REWARD_TYPE_DIFF ? ConstantType.user_money_log_source_type.type_33
			: ConstantType.user_money_log_source_type.type_34;
		int rewardSourceType = rewardType == REWARD_TYPE_DIRECT ? ConstantType.xms_reward_record_source_type.type_28
			: rewardType == REWARD_TYPE_DIFF ? ConstantType.xms_reward_record_source_type.type_29
			: ConstantType.xms_reward_record_source_type.type_30;
		int rows = userWalletService.handerUserMoney(rewardAmount, order.getOrderNo(), receiveUserId, order.getUserId(),
			moneySourceType, ConstantType.user_money_coin_type.type_1);
		if (rows != 1) {
			throw new ServiceException("托管团队奖励入账失败，userId=" + receiveUserId);
		}
		RewardRecord rewardRecord = new RewardRecord();
		rewardRecord.setOrderCode(IDUtils.getSnowflakeStr());
		rewardRecord.setUserId(receiveUserId);
		rewardRecord.setAmount(rewardAmount);
		rewardRecord.setCoinType(ConstantType.user_money_coin_type.type_1);
		rewardRecord.setSourceType(rewardSourceType);
		rewardRecord.setSourceOrderCode(order.getOrderNo());
		rewardRecord.setSourceUserId(order.getUserId());
		rewardRecord.setGtId(IDUtils.getSnowflakeStr());
		rewardRecord.setCreateTime(now);
		rewardRecordService.save(rewardRecord);
		saveSettlement(order, receiveUserId, rewardType, rewardLevel, rewardBase, ratioPercent, rewardAmount,
			grossReward, serviceFeeRatio, serviceFee, netReward, ARRIVAL_YES, null, rewardDay, now);
	}

	private void saveSettlement(StakeHostingOrder order, Long receiveUserId, int rewardType, Integer rewardLevel,
								BigDecimal rewardBase, BigDecimal ratioPercent, BigDecimal rewardAmount,
								BigDecimal grossReward, BigDecimal serviceFeeRatio, BigDecimal serviceFee,
								BigDecimal netReward, int arrivalStatus, Integer skipReason, int rewardDay, Date now) {
		StakeHostingRewardSettlement settlement = new StakeHostingRewardSettlement();
		settlement.setSettlementNo(IDUtils.getSnowflakeStr());
		settlement.setSourceOrderId(order.getId());
		settlement.setSourceOrderNo(order.getOrderNo());
		settlement.setSourceUserId(order.getUserId());
		settlement.setReceiveUserId(receiveUserId);
		settlement.setRewardType(rewardType);
		settlement.setRewardLevel(rewardLevel);
		settlement.setRewardBaseAmount(rewardBase);
		settlement.setRewardRatio(ratioPercent);
		settlement.setRewardAmount(rewardAmount);
		settlement.setGrossStaticReward(grossReward);
		settlement.setServiceFeeRatio(serviceFeeRatio);
		settlement.setServiceFeeAmount(serviceFee);
		settlement.setNetStaticReward(netReward);
		settlement.setArrivalStatus(arrivalStatus);
		settlement.setSkipReason(skipReason);
		settlement.setSettlementDay(rewardDay);
		settlement.setCreateTime(now);
		stakeHostingRewardSettlementService.save(settlement);
	}
}
