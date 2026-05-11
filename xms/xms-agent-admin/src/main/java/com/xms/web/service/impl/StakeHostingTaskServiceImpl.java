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
import com.xms.dao.domain.StakeHostingDailyTeamPerformance;
import com.xms.dao.domain.StakeHostingAfiPledge;
import com.xms.dao.domain.StakeHostingGlobalDividendBatch;
import com.xms.dao.domain.StakeHostingGlobalDividendDetail;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.domain.StakeHostingRewardSettlement;
import com.xms.dao.domain.StakeHostingUserRewardSummary;
import com.xms.dao.domain.StakeHostingWeeklyCommunityPerformance;
import com.xms.dao.domain.UserLevelConfig;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.vo.ParentUserTaskVo;
import com.xms.dao.service.IRewardRecordService;
import com.xms.dao.service.IStakeHostingAfiPledgeService;
import com.xms.dao.service.IStakeHostingDailyTeamPerformanceService;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IStakeHostingRewardSettlementService;
import com.xms.dao.service.IStakeHostingGlobalDividendPoolService;
import com.xms.dao.service.IStakeHostingGlobalDividendBatchService;
import com.xms.dao.service.IStakeHostingGlobalDividendDetailService;
import com.xms.dao.service.IStakeHostingUserRewardSummaryService;
import com.xms.dao.service.IStakeHostingWeeklyCommunityPerformanceService;
import com.xms.dao.service.ISysParaService;
import com.xms.dao.service.IUserLevelConfigService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import com.xms.dao.service.impl.StakeHostingAfiPledgeServiceImpl;
import com.xms.dao.service.impl.StakeHostingOrderServiceImpl;
import com.xms.dao.service.impl.StakeHostingWeeklyCommunityPerformanceServiceImpl;
import com.xms.web.service.IAsyncTaskService;
import com.xms.web.service.IStakeHostingTaskService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 托管定时任务Service实现
 */
@Slf4j
@Service
@AllArgsConstructor
public class StakeHostingTaskServiceImpl implements IStakeHostingTaskService {
	/**
	 * G7快照缺失时的兜底静态收益率：0.5%。
	 */
	private static final BigDecimal PLACEHOLDER_STATIC_RATE = new BigDecimal("0.005");
	private static final boolean FORCE_TEST_STATIC_RATE = Boolean.parseBoolean("true");
	private static final BigDecimal TEST_STATIC_RATE_PERCENT = new BigDecimal("1");
	private static final BigDecimal PURE_STATIC_RATE_BEFORE_RETURN_PERCENT = new BigDecimal("0.5");
	private static final BigDecimal PURE_STATIC_RATE_AFTER_RETURN_PERCENT = new BigDecimal("0.2");
	private static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");

	private final IStakeHostingOrderService stakeHostingOrderService;
	private final IStakeHostingDailyTeamPerformanceService stakeHostingDailyTeamPerformanceService;
	private final UserWalletService userWalletService;
	private final IRewardRecordService rewardRecordService;
	private final IAsyncTaskService asyncTaskServiceImpl;
	private final IStakeHostingAfiPledgeService stakeHostingAfiPledgeService;
	private final IStakeHostingRewardSettlementService stakeHostingRewardSettlementService;
	private final IStakeHostingGlobalDividendPoolService stakeHostingGlobalDividendPoolService;
	private final IStakeHostingGlobalDividendBatchService stakeHostingGlobalDividendBatchService;
	private final IStakeHostingGlobalDividendDetailService stakeHostingGlobalDividendDetailService;
	private final IStakeHostingUserRewardSummaryService stakeHostingUserRewardSummaryService;
	private final IStakeHostingWeeklyCommunityPerformanceService stakeHostingWeeklyCommunityPerformanceService;
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
	private static final int GLOBAL_DIVIDEND_BATCH_PROCESSING = 0;
	private static final int GLOBAL_DIVIDEND_BATCH_FINISHED = 1;
	private static final int WEEKLY_PERFORMANCE_SETTLED = 2;

	/**
	 * 每日发放托管订单静态收益。
	 *
	 * <p>101 任务按自然日 yyyyMMdd 结算：先为待发订单用户准备当天 G7 收益率快照，再逐笔订单计算
	 * 基础静态毛收益、AFI 加速后的实际毛收益、服务费和用户到账净收益。</p>
	 *
	 * <p>每笔奖励结算明细会保存基础静态收益率、AFI 加速倍率和实际静态收益率，便于测试和后续追溯
	 * “这笔订单当时到底按多少收益率结算”。当天累计服务费会汇总写入全球分红奖池流水。</p>
	 *
	 * <p>任务级幂等由 101 任务记录控制；当前本地测试阶段暂未恢复订单级 lastRewardDay 过滤和最终
	 * addDailyTask 写入，便于手动重复验证发放链路。</p>
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
		List<Long> rewardUserIds = orderList.stream()
			.map(StakeHostingOrder::getUserId)
			.distinct()
			.collect(Collectors.toList());
		stakeHostingDailyTeamPerformanceService.prepareDailySnapshots(rewardDay, rewardUserIds);
		Date now = new Date();
		BigDecimal dailyServiceFee = BigDecimal.ZERO;
		List<StaticRewardResult> staticRewardResults = new ArrayList<>(orderList.size());
		for (StakeHostingOrder order : orderList) {
			// 逐笔订单只做收益计算、结算明细和订单状态更新；钱包和奖励记录先收集，循环结束后统一批量落库。
			StaticRewardResult result = distributeOne(order, rewardDay, now);
			staticRewardResults.add(result);
			dailyServiceFee = dailyServiceFee.add(result.serviceFee);
		}
		// 静态收益结算明细也先收集后批量落库，避免在订单循环中逐笔写结算明细。
		saveStaticRewardSettlements(staticRewardResults);
		// 静态收益统一批量增加用户USDT余额，并批量保存RewardRecord，避免在订单循环里逐笔写钱包。
		grantStaticRewards(staticRewardResults, now);
		TeamRewardCollectContext teamRewardContext = new TeamRewardCollectContext();
		for (StaticRewardResult result : staticRewardResults) {
			if (result.shouldDistributeTeamReward()) {
				// 静态收益入账结果确定后，再基于本次计算出的净收益收集团队奖励结果。
				distributeTeamReward(result.order, result.grossReward, result.baseStaticRate, result.afiAccelerateRate,
					result.actualStaticRate, result.serviceFeeRatio, result.serviceFee, result.netReward, rewardDay, now,
					teamRewardContext);
			}
		}
		// 直推、极差、平级奖励统一批量落库：钱包、奖励记录、结算明细和团队收益汇总。
		flushTeamRewardContext(teamRewardContext);
		// 101 每日静态收益任务扣出的服务费不逐单入池，这里按当天累计服务费写一笔全球分红奖池收入流水。
		stakeHostingGlobalDividendPoolService.incomeDailyServiceFee(rewardDay,
			dailyServiceFee.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew), "task101");
		// 所有订单处理完成后再写入任务记录，避免中途失败造成当天任务被误标记完成。
		// 本地调试先注释
		//addDailyTask(strDate);
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
	 * 每周发放托管全球分红。
	 *
	 * <p>当前先按用户现有小区业绩作为权重，后续每周新增业绩快照确认后再替换权重来源。</p>
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void distributeWeeklyGlobalDividend() {
		String strDate = DateUtil.format(DateUtil.date(), "yyyyMMdd");
		int settlementDay = Integer.parseInt(strDate);
		Map<String, Object> task = asyncTaskServiceImpl.getTask(SysConstant.TSK_TYPE_102, strDate);
		if (!CollectionUtil.isEmpty(task)) {
			log.debug("任务类型102 每周托管全球分红任务已存在跳过");
			return;
		}
		BigDecimal poolAmount = stakeHostingGlobalDividendPoolService.getOrInitPool().getBalanceAmount();
		if (poolAmount == null || poolAmount.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("托管全球分红：奖池余额为0，跳过发放");
			addWeeklyTask(strDate);
			return;
		}

		Date now = new Date();
		String batchNo = IDUtils.getSnowflakeStr();
		StakeHostingGlobalDividendBatch batch = new StakeHostingGlobalDividendBatch();
		batch.setBatchNo(batchNo);
		batch.setSettlementDay(settlementDay);
		Date weekReference = DateUtil.offsetDay(now, -1);
		Long referenceTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.formatDate(weekReference);
		Long weekStartTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.weekStartTimeOf(referenceTime);
		Long weekEndTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.weekEndTimeOf(referenceTime);
		Date weekStartDate = DateUtil.parse(String.valueOf(weekStartTime), "yyyyMMddHHmmss");
		Date weekEndDate = DateUtil.parse(String.valueOf(weekEndTime), "yyyyMMddHHmmss");
		batch.setPeriodStartTime(weekStartDate);
		batch.setPeriodEndTime(weekEndDate);
		batch.setPlanAmount(poolAmount);
		batch.setActualAmount(BigDecimal.ZERO);
		batch.setUserCount(0);
		batch.setStatus(GLOBAL_DIVIDEND_BATCH_PROCESSING);
		batch.setCreateTime(now);
		stakeHostingGlobalDividendBatchService.save(batch);

		List<StakeHostingGlobalDividendDetail> details = buildGlobalDividendDetails(batchNo, poolAmount, weekStartTime);
		BigDecimal actualAmount = BigDecimal.ZERO;
		for (StakeHostingGlobalDividendDetail detail : details) {
			actualAmount = actualAmount.add(detail.getRewardAmount());
		}
		actualAmount = actualAmount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if (actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("托管全球分红：无满足小区业绩权重的用户，batchNo={}", batchNo);
			finishGlobalDividendBatch(batch.getId(), actualAmount, 0, now);
			addWeeklyTask(strDate);
			return;
		}

		stakeHostingGlobalDividendDetailService.saveBatch(details);
		for (StakeHostingGlobalDividendDetail detail : details) {
			grantGlobalDividend(batchNo, detail, now);
		}
		stakeHostingGlobalDividendPoolService.expenseWeeklyDividend(batchNo, actualAmount, "task102");
		markWeeklyPerformanceSettled(batchNo, weekStartTime, details, now);
		finishGlobalDividendBatch(batch.getId(), actualAmount, details.size(), now);
		addWeeklyTask(strDate);
	}

	private void addWeeklyTask(String strDate) {
		int rows = asyncTaskServiceImpl.addTask(SysConstant.TSK_TYPE_102, strDate);
		if (rows != 1) {
			throw new RuntimeException("任务类型102 每周托管全球分红任务插入失败");
		}
	}

	private List<StakeHostingGlobalDividendDetail> buildGlobalDividendDetails(String batchNo, BigDecimal poolAmount, Long weekStartTime) {
		List<UserLevelConfig> configs = userLevelConfigService.lambdaQuery()
			.gt(UserLevelConfig::getLevel, 0)
			.gt(UserLevelConfig::getGlobalFeeDividendRatio, BigDecimal.ZERO)
			.list();
		if (CollectionUtil.isEmpty(configs)) {
			return new ArrayList<>();
		}
		List<StakeHostingWeeklyCommunityPerformance> performances = stakeHostingWeeklyCommunityPerformanceService.lambdaQuery()
			.eq(StakeHostingWeeklyCommunityPerformance::getWeekStartTime, weekStartTime)
			.eq(StakeHostingWeeklyCommunityPerformance::getDeleted, 0)
			.gt(StakeHostingWeeklyCommunityPerformance::getCommunityNewPerformance, BigDecimal.ZERO)
			.list();
		if (CollectionUtil.isEmpty(performances)) {
			return new ArrayList<>();
		}
		Map<Long, StakeHostingWeeklyCommunityPerformance> performanceMap = performances.stream()
			.collect(Collectors.toMap(StakeHostingWeeklyCommunityPerformance::getUserId, Function.identity(), (a, b) -> a));
		List<UserInfo> users = userInfoService.lambdaQuery()
			.eq(UserInfo::getIsValid, 1)
			.in(UserInfo::getUserId, new ArrayList<>(performanceMap.keySet()))
			.list();
		if (CollectionUtil.isEmpty(users)) {
			return new ArrayList<>();
		}
		Map<Integer, List<UserInfo>> userMap = users.stream()
			.filter(user -> effectiveLevel(user) > 0)
			.collect(Collectors.groupingBy(this::effectiveLevel));
		List<StakeHostingGlobalDividendDetail> details = new ArrayList<>();
		for (UserLevelConfig config : configs) {
			List<UserInfo> levelUsers = userMap.get(config.getLevel());
			if (CollectionUtil.isEmpty(levelUsers)) {
				continue;
			}
			BigDecimal levelPool = poolAmount.multiply(config.getGlobalFeeDividendRatio())
				.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			if (levelPool.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			BigDecimal levelPerformance = levelUsers.stream()
				.map(user -> weeklyCommunityPerformance(performanceMap, user.getUserId()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			if (levelPerformance.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			for (UserInfo user : levelUsers) {
				BigDecimal userPerformance = weeklyCommunityPerformance(performanceMap, user.getUserId());
				BigDecimal rewardAmount = levelPool.multiply(userPerformance)
					.divide(levelPerformance, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
				if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
				StakeHostingGlobalDividendDetail detail = new StakeHostingGlobalDividendDetail();
				detail.setBatchNo(batchNo);
				detail.setUserId(user.getUserId());
				detail.setAccount(user.getAccount());
				detail.setRewardLevel(config.getLevel());
				detail.setLevelDividendRatio(config.getGlobalFeeDividendRatio());
				detail.setLevelPoolAmount(levelPool);
				detail.setUserCommunityPerformance(userPerformance);
				detail.setLevelCommunityPerformance(levelPerformance);
				detail.setRewardAmount(rewardAmount);
				detail.setCreateTime(new Date());
				details.add(detail);
			}
		}
		return details;
	}

	private BigDecimal weeklyCommunityPerformance(Map<Long, StakeHostingWeeklyCommunityPerformance> performanceMap, Long userId) {
		StakeHostingWeeklyCommunityPerformance performance = performanceMap.get(userId);
		if (performance == null || performance.getCommunityNewPerformance() == null) {
			return BigDecimal.ZERO;
		}
		return performance.getCommunityNewPerformance();
	}

	private void markWeeklyPerformanceSettled(String batchNo, Long weekStartTime, List<StakeHostingGlobalDividendDetail> details, Date now) {
		if (CollectionUtil.isEmpty(details)) {
			return;
		}
		List<Long> userIds = details.stream()
			.map(StakeHostingGlobalDividendDetail::getUserId)
			.collect(Collectors.toList());
		stakeHostingWeeklyCommunityPerformanceService.lambdaUpdate()
			.eq(StakeHostingWeeklyCommunityPerformance::getWeekStartTime, weekStartTime)
			.in(StakeHostingWeeklyCommunityPerformance::getUserId, userIds)
			.set(StakeHostingWeeklyCommunityPerformance::getSettleStatus, WEEKLY_PERFORMANCE_SETTLED)
			.set(StakeHostingWeeklyCommunityPerformance::getBatchNo, batchNo)
			.set(StakeHostingWeeklyCommunityPerformance::getUpdateTime, now)
			.update();
	}

	private void grantGlobalDividend(String batchNo, StakeHostingGlobalDividendDetail detail, Date now) {
		int rows = userWalletService.handerUserMoney(detail.getRewardAmount(), batchNo, detail.getUserId(), detail.getUserId(),
			ConstantType.user_money_log_source_type.type_37, ConstantType.user_money_coin_type.type_1);
		if (rows != 1) {
			throw new ServiceException("托管全球分红入账失败，userId=" + detail.getUserId());
		}
		RewardRecord rewardRecord = new RewardRecord();
		rewardRecord.setOrderCode(IDUtils.getSnowflakeStr());
		rewardRecord.setUserId(detail.getUserId());
		rewardRecord.setAmount(detail.getRewardAmount());
		rewardRecord.setCoinType(ConstantType.user_money_coin_type.type_1);
		rewardRecord.setSourceType(ConstantType.xms_reward_record_source_type.type_31);
		rewardRecord.setSourceOrderCode(batchNo);
		rewardRecord.setSourceUserId(detail.getUserId());
		rewardRecord.setGtId(IDUtils.getSnowflakeStr());
		rewardRecord.setCreateTime(now);
		rewardRecordService.save(rewardRecord);
		stakeHostingUserRewardSummaryService.addGlobalDividend(detail.getUserId(), detail.getRewardAmount());
	}

	private void finishGlobalDividendBatch(Long batchId, BigDecimal actualAmount, int userCount, Date now) {
		StakeHostingGlobalDividendBatch update = new StakeHostingGlobalDividendBatch();
		update.setId(batchId);
		update.setActualAmount(actualAmount);
		update.setUserCount(userCount);
		update.setStatus(GLOBAL_DIVIDEND_BATCH_FINISHED);
		update.setUpdateTime(now);
		stakeHostingGlobalDividendBatchService.updateById(update);
	}

	/**
	 * 给单笔产出中的托管订单发放一次静态收益。
	 *
	 * <p>主要步骤：</p>
	 * <p>1. 按当前静态收益率计算本次收益，并叠加已生效 AFI 加速倍率。</p>
	 * <p>2. 收益入账用户 USDT 钱包，并写入奖励记录。</p>
	 * <p>3. 累加订单已发收益和运行天数，更新最近发放日期。</p>
	 * <p>4. 判断是否回本、是否达到套餐天数。</p>
	 * <p>5. 订单发满后改为已完成，扣减对应托管业绩，并自动退还绑定的 AFI。</p>
	 *
	 * @param order 本次待发放的产出中托管订单
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param now 本次任务执行时间
	 * @return 本订单本次静态收益计算结果，用于批量入账和后续团队奖励发放
	 */
	private StaticRewardResult distributeOne(StakeHostingOrder order, int rewardDay, Date now) {
		// 先确定本订单当天基础静态收益率；当前测试阶段可能被硬编码为1%，正式逻辑会走用户指定收益率/G7快照。
		BigDecimal todayRate = calculateStaticRate(order, rewardDay);
		// 基础静态毛收益：托管本金 × 基础静态收益率，未扣服务费、未做用户到账。
		BigDecimal baseGrossReward = order.getStakeUsdtAmount().multiply(todayRate)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 查询该订单当天已经生效且未退还的AFI质押记录，AFI倍率使用质押当时保存的历史快照。
		StakeHostingAfiPledge effectiveAfiPledge = getEffectiveAfiPledge(order.getId(), rewardDay);
		BigDecimal afiAccelerateRate = getAfiAccelerateRate(effectiveAfiPledge);
		// 实际静态毛收益：基础静态毛收益 × AFI加速倍率；无AFI时倍率为1。
		BigDecimal grossReward = applyAfiAccelerate(baseGrossReward, afiAccelerateRate);
		// 收益率快照写入结算明细，便于后台追溯该订单当时按多少日利率结算。
		BigDecimal baseStaticRate = rateToPercent(todayRate);
		BigDecimal actualStaticRate = calculateActualStaticRate(todayRate, afiAccelerateRate);
		// 服务费按套餐快照比例从实际静态毛收益中扣除；剩余部分才是用户USDT钱包到账静态收益。
		BigDecimal serviceFeeRatio = getServiceFeeRatio(order);
		BigDecimal serviceFee = grossReward.multiply(serviceFeeRatio)
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal reward = grossReward.subtract(serviceFee)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		int nextRunDays = order.getRunDays() + 1;
		BigDecimal totalReward = order.getTotalStaticReward()
			.add(reward)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);

		// 构造静态收益结算明细，记录毛收益、服务费、净收益、基础收益率、AFI倍率和实际收益率快照，稍后统一批量落库。
		StakeHostingRewardSettlement staticSettlement = buildSettlement(order, null, REWARD_TYPE_STATIC_FEE, null, grossReward, serviceFeeRatio,
			serviceFee, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, reward, ARRIVAL_YES, null, rewardDay, now);

		// 回写订单收益进度：今日收益、累计收益、已运行天数、最近发放日和是否回本。
		StakeHostingOrder update = new StakeHostingOrder();
		update.setId(order.getId());
		update.setTodayReward(reward);
		update.setTotalStaticReward(totalReward);
		update.setRunDays(nextRunDays);
		update.setLastRewardDay(rewardDay);
		update.setIsReturnPrincipal(totalReward.compareTo(order.getStakeUsdtAmount()) >= 0 ? 1 : 0);
		update.setUpdateTime(now);
		// 已发放次数达到套餐天数时，订单本次101任务内直接完成。
		boolean finished = nextRunDays >= order.getPackageDays();
		if (finished) {
			update.setStatus(StakeHostingOrderServiceImpl.STATUS_FINISHED);
			update.setFinishTime(now);
		}
		if (!stakeHostingOrderService.updateById(update)) {
			throw new ServiceException("更新托管订单收益失败，orderNo=" + order.getOrderNo());
		}
		if (finished) {
			// 订单完成后扣减用户托管业绩，保持个人/团队托管业绩口径随订单生命周期变化。
			stakeHostingOrderService.subtractHostingPerformance(order.getUserId(), order.getStakeUsdtAmount(), order.getId());
			// 若订单绑定AFI质押且未退还，则到期自动退回用户AFI余额并写AFI退还钱包流水。
			stakeHostingAfiPledgeService.returnPledgeByOrderId(order.getId());
			// G7团队TVL到期扣减记到次日，避免影响本次已按当天快照计算完成的静态收益率。
			stakeHostingDailyTeamPerformanceService.recordOrderTeamExpiredAmountNextDay(order.getId(), rewardDay);
		}
		// 返回本订单完整静态收益计算结果，调用方统一批量入账静态收益并继续发放团队奖励。
		return new StaticRewardResult(order, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, reward, staticSettlement);
	}

	/**
	 * 批量保存101任务产生的静态收益结算明细。
	 *
	 * @param results 本轮101任务已计算完成的静态收益结果
	 */
	private void saveStaticRewardSettlements(List<StaticRewardResult> results) {
		if (CollectionUtil.isEmpty(results)) {
			return;
		}
		List<StakeHostingRewardSettlement> settlements = results.stream()
			.map(result -> result.staticSettlement)
			.collect(Collectors.toList());
		stakeHostingRewardSettlementService.saveBatch(settlements);
	}

	/**
	 * 批量发放本轮101任务计算出的静态净收益。
	 *
	 * <p>订单循环阶段只负责计算每笔订单应得金额；这里统一构造钱包增加记录和奖励记录，
	 * 按1000条一批批量更新USDT余额并批量保存RewardRecord，避免在订单循环中逐笔写钱包。</p>
	 *
	 * @param results 本轮101任务已计算完成的静态收益结果
	 * @param now 本次任务执行时间
	 */
	private void grantStaticRewards(List<StaticRewardResult> results, Date now) {
		if (CollectionUtil.isEmpty(results)) {
			return;
		}
		int batchSize = 1000;
		List<UserMoney> userMoneyList = new ArrayList<>(Math.min(results.size(), batchSize));
		List<RewardRecord> rewardRecordList = new ArrayList<>(Math.min(results.size(), batchSize));
		for (StaticRewardResult result : results) {
			if (result.netReward.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			String gtId = IDUtils.getSnowflakeStr();
			// 静态净收益入用户USDT钱包，钱包流水source_type=31。
			UserMoney userMoney = new UserMoney();
			userMoney.setId(result.order.getUserId());
			userMoney.setValidNum1(result.netReward);
			userMoney.setGtId(gtId);
			userMoney.setSourceCode(result.order.getOrderNo());
			userMoney.setSourceId(result.order.getUserId());
			userMoney.setSourceType(ConstantType.user_money_log_source_type.type_31);
			userMoney.setUpdateTime(now);
			userMoneyList.add(userMoney);

			// 记录用户静态收益奖励记录，gtId与钱包变动保持一致，方便后续追溯。
			RewardRecord rewardRecord = new RewardRecord();
			rewardRecord.setOrderCode(IDUtils.getSnowflakeStr());
			rewardRecord.setUserId(result.order.getUserId());
			rewardRecord.setAmount(result.netReward);
			rewardRecord.setCoinType(ConstantType.user_money_coin_type.type_1);
			rewardRecord.setSourceType(ConstantType.xms_reward_record_source_type.type_27);
			rewardRecord.setSourceOrderCode(result.order.getOrderNo());
			rewardRecord.setSourceUserId(result.order.getUserId());
			rewardRecord.setGtId(gtId);
			rewardRecord.setCreateTime(now);
			rewardRecordList.add(rewardRecord);

			if (userMoneyList.size() >= batchSize) {
				flushStaticRewardBatch(userMoneyList, rewardRecordList);
			}
		}
		flushStaticRewardBatch(userMoneyList, rewardRecordList);
	}

	/**
	 * 批量落库静态收益钱包变动和奖励记录。
	 *
	 * @param userMoneyList 待批量增加USDT余额的钱包记录
	 * @param rewardRecordList 待批量保存的静态收益奖励记录
	 */
	private void flushStaticRewardBatch(List<UserMoney> userMoneyList, List<RewardRecord> rewardRecordList) {
		if (CollectionUtil.isNotEmpty(userMoneyList)) {
			int rows = userWalletService.batchUpdateUserMoney(userMoneyList);
			if (rows != userMoneyList.size()) {
				throw new ServiceException("批量发放托管静态收益入账失败");
			}
			userMoneyList.clear();
		}
		if (CollectionUtil.isNotEmpty(rewardRecordList)) {
			rewardRecordService.saveBatch(rewardRecordList);
			rewardRecordList.clear();
		}
	}

	/**
	 * 单笔托管订单在101任务中的静态收益计算结果。
	 *
	 * <p>该对象只保存已经算好的金额和收益率快照，后续用于批量静态入账以及团队奖励发放，
	 * 避免在订单循环中边计算边逐笔写钱包。</p>
	 */
	private static class StaticRewardResult {
		private final StakeHostingOrder order;
		private final BigDecimal grossReward;
		private final BigDecimal baseStaticRate;
		private final BigDecimal afiAccelerateRate;
		private final BigDecimal actualStaticRate;
		private final BigDecimal serviceFeeRatio;
		private final BigDecimal serviceFee;
		private final BigDecimal netReward;
		private final StakeHostingRewardSettlement staticSettlement;

		private StaticRewardResult(StakeHostingOrder order, BigDecimal grossReward, BigDecimal baseStaticRate,
								   BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
								   BigDecimal serviceFeeRatio, BigDecimal serviceFee, BigDecimal netReward,
								   StakeHostingRewardSettlement staticSettlement) {
			this.order = order;
			this.grossReward = grossReward;
			this.baseStaticRate = baseStaticRate;
			this.afiAccelerateRate = afiAccelerateRate;
			this.actualStaticRate = actualStaticRate;
			this.serviceFeeRatio = serviceFeeRatio;
			this.serviceFee = serviceFee;
			this.netReward = netReward;
			this.staticSettlement = staticSettlement;
		}

		/**
		 * 判断本笔静态收益是否需要继续触发团队奖励。
		 *
		 * @return true表示用户购买订单且本次静态净收益大于0
		 */
		private boolean shouldDistributeTeamReward() {
			return order.getSourceType() != null
				&& order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_USER
				&& netReward.compareTo(BigDecimal.ZERO) > 0;
		}
	}

	/**
	 * 查询当前收益日可用于加速的 AFI 质押记录。
	 *
	 * <p>AFI质押状态只表示是否已退还：1=生效中，2=已退还。
	 * 是否参与当天收益加速由 effectiveDay 控制，只有 effectiveDay <= rewardDay 的记录才参与加速。</p>
	 *
	 * @param orderId 托管订单ID
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @return 当天可用于收益加速的AFI质押记录；没有则返回null
	 */
	private StakeHostingAfiPledge getEffectiveAfiPledge(Long orderId, int rewardDay) {
		if (orderId == null) {
			return null;
		}
		// 只查状态为1=生效中的质押记录；是否“今天能加速”由effectiveDay和rewardDay判断。
		return stakeHostingAfiPledgeService.lambdaQuery()
			.eq(StakeHostingAfiPledge::getStakeHostingOrderId, orderId)
			.le(StakeHostingAfiPledge::getEffectiveDay, rewardDay)
			.eq(StakeHostingAfiPledge::getStatus, StakeHostingAfiPledgeServiceImpl.STATUS_EFFECTIVE)
			.one();
	}

	/**
	 * 读取 AFI 质押记录里的加速倍率快照。
	 *
	 * <p>倍率来自用户质押当时命中的配置快照，后续后台配置调整不会影响历史订单收益。</p>
	 *
	 * @param pledge 已生效的 AFI 质押记录，可为空
	 * @return AFI 加速倍率；无有效质押时返回1
	 */
	private BigDecimal getAfiAccelerateRate(StakeHostingAfiPledge pledge) {
		if (pledge == null || pledge.getAccelerateRate() == null || pledge.getAccelerateRate().compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ONE;
		}
		return pledge.getAccelerateRate();
	}

	/**
	 * 按 AFI 加速倍率计算实际静态毛收益。
	 *
	 * @param baseGrossReward 基础静态毛收益，单位USDT
	 * @param afiAccelerateRate AFI 加速倍率，无加速时为1
	 * @return 加速后的静态毛收益，单位USDT
	 */
	private BigDecimal applyAfiAccelerate(BigDecimal baseGrossReward, BigDecimal afiAccelerateRate) {
		return baseGrossReward.multiply(afiAccelerateRate)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	/**
	 * 计算单笔托管订单当天使用的基础静态收益率。
	 *
	 * <p>当前临时硬编码测试为所有订单返回 1% 日利率；恢复正式逻辑时删除方法开头的测试返回值。
	 * 正式收益率优先级：用户指定收益率 > 未推广特殊规则 > G7团队TVL快照。
	 * 配置和快照里的收益率单位是%，本方法返回实际乘数，例如 0.5% 返回 0.005。</p>
	 *
	 * @param order 托管订单
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @return 基础静态收益率乘数
	 */
	private BigDecimal calculateStaticRate(StakeHostingOrder order, int rewardDay) {
		// 临时测试代码：所有托管订单静态日利率固定为1%，测完恢复正式收益率逻辑。
		if (FORCE_TEST_STATIC_RATE) {
			return percentToRate(TEST_STATIC_RATE_PERCENT);
		}

		UserInfo user = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, order.getUserId())
			.one();
		if (user != null && user.getStakeHostingStaticRate() != null && user.getStakeHostingStaticRate().compareTo(BigDecimal.ZERO) > 0) {
			return percentToRate(user.getStakeHostingStaticRate());
		}
		if (!stakeHostingDailyTeamPerformanceService.hasTeamTvl(order.getUserId(), rewardDay)) {
			BigDecimal pureStaticRate = order.getIsReturnPrincipal() != null && order.getIsReturnPrincipal() == 1
				? PURE_STATIC_RATE_AFTER_RETURN_PERCENT : PURE_STATIC_RATE_BEFORE_RETURN_PERCENT;
			return percentToRate(pureStaticRate);
		}
		StakeHostingDailyTeamPerformance snapshot = stakeHostingDailyTeamPerformanceService.getCalculatedSnapshot(order.getUserId(), rewardDay);
		if (snapshot == null || snapshot.getBaseStaticRate() == null) {
			return PLACEHOLDER_STATIC_RATE;
		}
		return percentToRate(snapshot.getBaseStaticRate());
	}

	/**
	 * 将百分比收益率换算成实际乘数。
	 *
	 * @param percentRate 百分比收益率，例如0.5表示0.5%
	 * @return 实际乘数，例如0.005
	 */
	private BigDecimal percentToRate(BigDecimal percentRate) {
		return percentRate.divide(PERCENT_DIVISOR, 8, ConstantStatic.roundingModeNew);
	}

	/**
	 * 将静态收益率乘数转换为百分比快照。
	 *
	 * @param rate 静态收益率乘数，例如0.005
	 * @return 百分比收益率，例如0.5000表示0.5%
	 */
	private BigDecimal rateToPercent(BigDecimal rate) {
		return rate.multiply(PERCENT_DIVISOR)
			.setScale(4, ConstantStatic.roundingModeNew);
	}

	/**
	 * 计算订单本次实际使用的静态收益率百分比。
	 *
	 * @param baseRate 基础静态收益率乘数，例如0.005
	 * @param afiAccelerateRate AFI 加速倍率，无加速时为1
	 * @return 实际静态收益率百分比，例如0.7500表示0.75%
	 */
	private BigDecimal calculateActualStaticRate(BigDecimal baseRate, BigDecimal afiAccelerateRate) {
		return baseRate.multiply(afiAccelerateRate)
			.multiply(PERCENT_DIVISOR)
			.setScale(4, ConstantStatic.roundingModeNew);
	}

	/**
	 * 读取订单创建时保存的服务费比例快照。
	 *
	 * 服务费会影响用户到账收益和全球分红奖池入池金额，所以 101 结算不能回查当前套餐配置；
	 * 后台后续调整套餐服务费，只能影响新创建的托管订单。
	 *
	 * @param order 本次结算的托管订单
	 * @return 服务费比例快照，单位%
	 */
	private BigDecimal getServiceFeeRatio(StakeHostingOrder order) {
		if (order == null || order.getServiceFeeRatio() == null) {
			return BigDecimal.ZERO;
		}
		return order.getServiceFeeRatio();
	}

	/**
	 * 按订单静态净收益触发托管团队奖励链路。
	 *
	 * @param order 源托管订单
	 * @param grossReward AFI 加速后的静态毛收益，单位USDT
	 * @param baseStaticRate 基础静态收益率快照，单位%
	 * @param afiAccelerateRate AFI 加速倍率快照
	 * @param actualStaticRate 实际静态收益率快照，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额，单位USDT
	 * @param netReward 用户到账静态净收益，单位USDT
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param now 本次任务执行时间
	 * @param context 团队奖励批量收集上下文
	 */
	private void distributeTeamReward(StakeHostingOrder order, BigDecimal grossReward, BigDecimal baseStaticRate,
									  BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
									  BigDecimal serviceFeeRatio, BigDecimal serviceFee,
									  BigDecimal netReward, int rewardDay, Date now,
									  TeamRewardCollectContext context) {
		List<ParentUserTaskVo> parentUsers = userInfoService.getParentUserTaskVo(order.getUserId());
		if (CollectionUtil.isEmpty(parentUsers)) {
			log.info("托管团队奖励跳过：源订单无上级，orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
			return;
		}
		distributeDirectReward(order, parentUsers.get(0), grossReward, baseStaticRate, afiAccelerateRate,
			actualStaticRate, serviceFeeRatio, serviceFee, netReward, rewardDay, now, context);
		distributeDiffAndSameLevelReward(order, parentUsers, grossReward, baseStaticRate, afiAccelerateRate,
			actualStaticRate, serviceFeeRatio, serviceFee, netReward, rewardDay, now, context);
	}

	/**
	 * 发放直属上级直推奖，若直属上级无效或无有效托管订单则只写未到账结算明细。
	 *
	 * @param order 源托管订单
	 * @param directUser 直属上级，可为空
	 * @param grossReward AFI 加速后的静态毛收益，单位USDT
	 * @param baseStaticRate 基础静态收益率快照，单位%
	 * @param afiAccelerateRate AFI 加速倍率快照
	 * @param actualStaticRate 实际静态收益率快照，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额，单位USDT
	 * @param netReward 用户到账静态净收益，单位USDT
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param now 本次任务执行时间
	 * @param context 团队奖励批量收集上下文
	 */
	private void distributeDirectReward(StakeHostingOrder order, ParentUserTaskVo directUser, BigDecimal grossReward,
										BigDecimal baseStaticRate, BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
										BigDecimal serviceFeeRatio, BigDecimal serviceFee, BigDecimal netReward,
										int rewardDay, Date now, TeamRewardCollectContext context) {
		// 直推奖固定取直属上级，奖励基数使用用户本次到账静态净收益，而不是静态毛收益。
		BigDecimal directRatioPercent = getDirectRewardRatioPercent();
		BigDecimal directReward = calculateReward(netReward, directRatioPercent);
		if (directUser == null) {
			// 没有直属上级时，直推链路直接结束；按当前口径不写平台沉淀或未到账明细。
			log.info("托管直推奖励跳过：源订单无直属上级，orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
			return;
		}
		Integer skipReason = getRewardSkipReason(directUser);
		if (skipReason != null) {
			// 直属上级存在但无效或没有有效托管订单时，只写未到账结算明细，保留本应获得的直推金额和跳过原因。
			collectSkippedSettlement(context, order, directUser.getUserId(), REWARD_TYPE_PLATFORM, effectiveLevel(directUser), netReward, directRatioPercent,
				directReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
				serviceFeeRatio, serviceFee, netReward, ARRIVAL_NO, skipReason, rewardDay, now);
			return;
		}
		// 直属上级有效且有有效托管订单，先收集直推奖钱包、RewardRecord和到账结算明细，稍后统一批量落库。
		collectTeamReward(context, order, directUser.getUserId(), REWARD_TYPE_DIRECT, effectiveLevel(directUser), netReward, directRatioPercent,
			directReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, netReward, rewardDay, now);
	}

	/**
	 * 按上级链路发放极差奖和平级奖。
	 *
	 * <p>无效上级或无有效托管订单的上级会写未到账结算明细，但不推进已覆盖比例；
	 * 后续有效高等级上级仍可继续按未覆盖比例拿奖励。</p>
	 *
	 * @param order 源托管订单
	 * @param parentUsers 源用户上级链路，按近到远排序
	 * @param grossReward AFI 加速后的静态毛收益，单位USDT
	 * @param baseStaticRate 基础静态收益率快照，单位%
	 * @param afiAccelerateRate AFI 加速倍率快照
	 * @param actualStaticRate 实际静态收益率快照，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额，单位USDT
	 * @param netReward 用户到账静态净收益，单位USDT
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param now 本次任务执行时间
	 * @param context 团队奖励批量收集上下文
	 */
	private void distributeDiffAndSameLevelReward(StakeHostingOrder order, List<ParentUserTaskVo> parentUsers,
												  BigDecimal grossReward, BigDecimal baseStaticRate,
												  BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
												  BigDecimal serviceFeeRatio, BigDecimal serviceFee,
												  BigDecimal netReward, int rewardDay, Date now,
												  TeamRewardCollectContext context) {
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
						collectTeamReward(context, order, rewardUser.getUserId(), rewardType, level, netReward, diffRatio,
							rewardAmount, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
							serviceFeeRatio, serviceFee, netReward, rewardDay, now);
					} else {
						collectSkippedSettlement(context, order, rewardUser.getUserId(), REWARD_TYPE_PLATFORM, level, netReward, diffRatio,
							rewardAmount, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
							serviceFeeRatio, serviceFee, netReward, ARRIVAL_NO, skipReason, rewardDay, now);
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
	 * 暂无暂停托管订单流程；因此这里按“支付成功，且未完成”判断有效资格。</p>
	 */
	private boolean hasUnfinishedHostingOrder(Long userId) {
		return stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
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

	private int effectiveLevel(UserInfo user) {
		return Math.max(Math.max(defaultLevel(user.getGameLevel()), defaultLevel(user.getMinGameLevel())), defaultLevel(user.getAdminGameLevel()));
	}

	private int defaultLevel(Integer level) {
		return level == null ? 0 : level;
	}

	/**
	 * 收集团队奖励到账数据，等待本轮101任务统一批量落库。
	 *
	 * @param order 源托管订单
	 * @param receiveUserId 奖励接收用户ID
	 * @param rewardType 奖励类型：直推、极差或平级
	 * @param rewardLevel 本次奖励使用的F等级
	 * @param rewardBase 奖励计算基数，单位USDT
	 * @param ratioPercent 奖励比例，单位%
	 * @param context 团队奖励批量收集上下文
	 * @param rewardAmount 到账奖励金额，单位USDT
	 * @param grossReward AFI 加速后的静态毛收益，单位USDT
	 * @param baseStaticRate 基础静态收益率快照，单位%
	 * @param afiAccelerateRate AFI 加速倍率快照
	 * @param actualStaticRate 实际静态收益率快照，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额，单位USDT
	 * @param netReward 用户到账静态净收益，单位USDT
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param now 本次任务执行时间
	 */
	private void collectTeamReward(TeamRewardCollectContext context, StakeHostingOrder order, Long receiveUserId, int rewardType, Integer rewardLevel,
								   BigDecimal rewardBase, BigDecimal ratioPercent, BigDecimal rewardAmount,
								   BigDecimal grossReward, BigDecimal baseStaticRate,
								   BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
								   BigDecimal serviceFeeRatio, BigDecimal serviceFee,
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
		String gtId = IDUtils.getSnowflakeStr();
		// 团队奖励先收集钱包增量，后续统一批量增加USDT余额。
		UserMoney userMoney = new UserMoney();
		userMoney.setId(receiveUserId);
		userMoney.setValidNum1(rewardAmount);
		userMoney.setGtId(gtId);
		userMoney.setSourceCode(order.getOrderNo());
		userMoney.setSourceId(order.getUserId());
		userMoney.setSourceType(moneySourceType);
		userMoney.setUpdateTime(now);
		context.userMoneyList.add(userMoney);

		// 奖励记录与钱包流水共用gtId，便于追溯本次团队奖励入账。
		RewardRecord rewardRecord = new RewardRecord();
		rewardRecord.setOrderCode(IDUtils.getSnowflakeStr());
		rewardRecord.setUserId(receiveUserId);
		rewardRecord.setAmount(rewardAmount);
		rewardRecord.setCoinType(ConstantType.user_money_coin_type.type_1);
		rewardRecord.setSourceType(rewardSourceType);
		rewardRecord.setSourceOrderCode(order.getOrderNo());
		rewardRecord.setSourceUserId(order.getUserId());
		rewardRecord.setGtId(gtId);
		rewardRecord.setCreateTime(now);
		context.rewardRecordList.add(rewardRecord);
		collectTeamRewardSummary(context, receiveUserId, rewardType, rewardAmount);
		context.settlementList.add(buildSettlement(order, receiveUserId, rewardType, rewardLevel, rewardBase, ratioPercent, rewardAmount,
			grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, netReward, ARRIVAL_YES, null, rewardDay, now));
	}

	/**
	 * 收集团队奖励未到账结算明细。
	 *
	 * <p>无效上级或无有效托管订单的上级不发钱包、不写RewardRecord，只保留未到账结算明细用于追溯。</p>
	 */
	private void collectSkippedSettlement(TeamRewardCollectContext context, StakeHostingOrder order, Long receiveUserId,
										  int rewardType, Integer rewardLevel, BigDecimal rewardBase,
										  BigDecimal ratioPercent, BigDecimal rewardAmount,
										  BigDecimal grossReward, BigDecimal baseStaticRate,
										  BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
										  BigDecimal serviceFeeRatio, BigDecimal serviceFee,
										  BigDecimal netReward, int arrivalStatus, Integer skipReason, int rewardDay, Date now) {
		context.settlementList.add(buildSettlement(order, receiveUserId, rewardType, rewardLevel, rewardBase, ratioPercent,
			rewardAmount, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate, serviceFeeRatio,
			serviceFee, netReward, arrivalStatus, skipReason, rewardDay, now));
	}

	/**
	 * 收集团队收益汇总增量。
	 *
	 * <p>App团队收益只累计极差奖和平级奖，直推奖不计入该汇总。</p>
	 */
	private void collectTeamRewardSummary(TeamRewardCollectContext context, Long receiveUserId, int rewardType, BigDecimal rewardAmount) {
		if (rewardType != REWARD_TYPE_DIFF && rewardType != REWARD_TYPE_SAME_LEVEL) {
			return;
		}
		StakeHostingUserRewardSummary summary = context.summaryMap.computeIfAbsent(receiveUserId, userId -> {
			StakeHostingUserRewardSummary item = new StakeHostingUserRewardSummary();
			item.setUserId(userId);
			item.setDiffRewardAmount(BigDecimal.ZERO);
			item.setSameLevelRewardAmount(BigDecimal.ZERO);
			return item;
		});
		if (rewardType == REWARD_TYPE_DIFF) {
			summary.setDiffRewardAmount(summary.getDiffRewardAmount().add(rewardAmount));
		} else {
			summary.setSameLevelRewardAmount(summary.getSameLevelRewardAmount().add(rewardAmount));
		}
	}

	/**
	 * 批量落库本轮101任务产生的团队奖励结果。
	 *
	 * <p>包含直推、极差、平级奖励的钱包入账、RewardRecord、到账/未到账结算明细，以及极差/平级团队收益汇总。</p>
	 */
	private void flushTeamRewardContext(TeamRewardCollectContext context) {
		if (context == null || context.isEmpty()) {
			return;
		}
		if (CollectionUtil.isNotEmpty(context.userMoneyList)) {
			int rows = userWalletService.batchUpdateUserMoney(context.userMoneyList);
			if (rows != context.userMoneyList.size()) {
				throw new ServiceException("批量发放托管团队奖励入账失败");
			}
		}
		if (CollectionUtil.isNotEmpty(context.rewardRecordList)) {
			rewardRecordService.saveBatch(context.rewardRecordList);
		}
		if (CollectionUtil.isNotEmpty(context.settlementList)) {
			stakeHostingRewardSettlementService.saveBatch(context.settlementList);
		}
		if (!context.summaryMap.isEmpty()) {
			stakeHostingUserRewardSummaryService.batchAddTeamRewardSummary(new ArrayList<>(context.summaryMap.values()));
		}
	}

	/**
	 * 团队奖励收集上下文。
	 *
	 * <p>用于101任务批量结算收集-落库模式，避免在直推/极差/平级循环中逐笔写钱包和奖励记录。</p>
	 */
	private static class TeamRewardCollectContext {
		private final List<UserMoney> userMoneyList = new ArrayList<>();
		private final List<RewardRecord> rewardRecordList = new ArrayList<>();
		private final List<StakeHostingRewardSettlement> settlementList = new ArrayList<>();
		private final Map<Long, StakeHostingUserRewardSummary> summaryMap = new HashMap<>();

		/**
		 * 判断当前上下文是否没有任何待落库数据。
		 *
		 * @return true表示无待保存团队奖励数据
		 */
		private boolean isEmpty() {
			return userMoneyList.isEmpty() && rewardRecordList.isEmpty() && settlementList.isEmpty() && summaryMap.isEmpty();
		}
	}

	/**
	 * 保存托管奖励结算明细。
	 *
	 * <p>明细会同时保存基础静态收益率、AFI 加速倍率、实际静态收益率、服务费和净收益快照，
	 * 用于后台查询、导出以及测试追溯历史结算口径。</p>
	 *
	 * @param order 源托管订单
	 * @param receiveUserId 奖励接收用户ID；静态服务费结算可为空
	 * @param rewardType 奖励类型
	 * @param rewardLevel 奖励等级，可为空
	 * @param rewardBase 奖励计算基数，单位USDT
	 * @param ratioPercent 奖励比例，单位%
	 * @param rewardAmount 奖励金额，单位USDT
	 * @param grossReward AFI 加速后的静态毛收益，单位USDT
	 * @param baseStaticRate 基础静态收益率快照，单位%
	 * @param afiAccelerateRate AFI 加速倍率快照
	 * @param actualStaticRate 实际静态收益率快照，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额，单位USDT
	 * @param netReward 用户到账静态净收益，单位USDT
	 * @param arrivalStatus 到账状态
	 * @param skipReason 未到账原因，可为空
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param now 本次任务执行时间
	 */
	private void saveSettlement(StakeHostingOrder order, Long receiveUserId, int rewardType, Integer rewardLevel,
								BigDecimal rewardBase, BigDecimal ratioPercent, BigDecimal rewardAmount,
								BigDecimal grossReward, BigDecimal baseStaticRate,
								BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
								BigDecimal serviceFeeRatio, BigDecimal serviceFee,
								BigDecimal netReward, int arrivalStatus, Integer skipReason, int rewardDay, Date now) {
		stakeHostingRewardSettlementService.save(buildSettlement(order, receiveUserId, rewardType, rewardLevel,
			rewardBase, ratioPercent, rewardAmount, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, netReward, arrivalStatus, skipReason, rewardDay, now));
	}

	/**
	 * 构造托管奖励结算明细实体。
	 *
	 * <p>静态收益明细会先构造并收集到结果对象里，最终批量保存；团队奖励当前仍复用该构造逻辑后单笔保存。</p>
	 *
	 * @return 待保存的托管奖励结算明细
	 */
	private StakeHostingRewardSettlement buildSettlement(StakeHostingOrder order, Long receiveUserId, int rewardType, Integer rewardLevel,
														 BigDecimal rewardBase, BigDecimal ratioPercent, BigDecimal rewardAmount,
														 BigDecimal grossReward, BigDecimal baseStaticRate,
														 BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
														 BigDecimal serviceFeeRatio, BigDecimal serviceFee,
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
		settlement.setBaseStaticRate(baseStaticRate);
		settlement.setAfiAccelerateRate(afiAccelerateRate);
		settlement.setActualStaticRate(actualStaticRate);
		settlement.setServiceFeeRatio(serviceFeeRatio);
		settlement.setServiceFeeAmount(serviceFee);
		settlement.setNetStaticReward(netReward);
		settlement.setArrivalStatus(arrivalStatus);
		settlement.setSkipReason(skipReason);
		settlement.setSettlementDay(rewardDay);
		settlement.setCreateTime(now);
		return settlement;
	}
}
