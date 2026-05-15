package com.xms.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
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
import com.xms.dao.entity.dto.StakeHostingStaticRateTestDto;
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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
	private static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");
	private static final BigDecimal TWO = new BigDecimal("2");
	private static final String SQL_VALID_NUM1 = "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num1=valid_num1+?,source_code=?,source_type=?,source_id=? WHERE id=? ";

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
	private final JdbcTemplate jdbcTemplate;

	private static final int REWARD_TYPE_STATIC_FEE = 1;
	private static final int REWARD_TYPE_DIRECT = 2;
	private static final int REWARD_TYPE_DIFF = 3;
	private static final int REWARD_TYPE_SAME_LEVEL = 4;
	private static final int REWARD_TYPE_PLATFORM = 5;
	private static final int ARRIVAL_NO = 0;
	private static final int ARRIVAL_YES = 1;
	private static final int SKIP_NO_ACTIVE_ORDER = 2;
	private static final int GLOBAL_DIVIDEND_BATCH_PROCESSING = 0;
	private static final int GLOBAL_DIVIDEND_BATCH_FINISHED = 1;
	private static final int WEEKLY_PERFORMANCE_SETTLED = 2;
	private static final int G7_CALC_STATUS_DONE = 1;
	private static final int DELETED_NO = 0;

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
		StaticRewardCalculateContext staticContext = buildStaticRewardCalculateContext(orderList, rewardDay);
		Date now = new Date();
		BigDecimal dailyServiceFee = BigDecimal.ZERO;
		List<StaticRewardResult> staticRewardResults = new ArrayList<>(orderList.size());
		for (StakeHostingOrder order : orderList) {
			// 逐笔订单只做收益计算、结算明细和订单状态更新；钱包和奖励记录先收集，循环结束后统一批量落库。
			StaticRewardResult result = distributeOne(order, rewardDay, now, staticContext);
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
		// 本轮所有静态和团队奖励都计算落库后，再统一处理到期订单后置动作，避免中间态影响奖励口径。
		handleFinishedOrdersAfterRewards(staticRewardResults, now);
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
	 * <p>102任务按上一自然周的周新增小区业绩快照发放USDT全球分红：
	 * 先读取当前全球分红奖池余额生成分红批次，再按等级配置 `global_fee_dividend_ratio` 切分等级奖池，
	 * 同等级内只使用 `community_new_performance > 0` 的用户按占比分配。发放成功后会写分红明细、
	 * 增加用户USDT钱包、写RewardRecord、扣减奖池余额并标记周业绩记录已参与分红。</p>
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void distributeWeeklyGlobalDividend() {
		// 1. 102任务按结算日做日级幂等，避免同一天重复执行每周全球分红。
		String strDate = DateUtil.format(DateUtil.date(), "yyyyMMdd");
		int settlementDay = Integer.parseInt(strDate);
		Map<String, Object> task = asyncTaskServiceImpl.getTask(SysConstant.TSK_TYPE_102, strDate);
		if (!CollectionUtil.isEmpty(task)) {
			log.debug("任务类型102 每周托管全球分红任务已存在跳过");
			return;
		}
		// 2. 读取当前全球分红奖池余额；没有余额时只记录任务完成，不生成批次和明细。
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
		// 3. 任务通常在周日24点/周一0点附近执行，用昨天作为参考日锁定“上一自然周”的统计周期。
		Date weekReference = DateUtil.offsetDay(now, -1);
		// 将参考日转为周业绩统一使用的 long 时间格式，例如 20260517235959。
		Long referenceTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.formatDate(weekReference);
		// 根据参考日定位本次分红使用的自然周开始时间：周一 00:00:00，格式yyyyMMddHHmmss。
		Long weekStartTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.weekStartTimeOf(referenceTime);
		// 根据参考日定位本次分红使用的自然周结束时间：周日 23:59:59，格式yyyyMMddHHmmss。
		Long weekEndTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.weekEndTimeOf(referenceTime);
		// 批次表使用 Date 类型展示分红周期，所以把 long 格式的周开始时间转回 Date。
		Date weekStartDate = DateUtil.parse(String.valueOf(weekStartTime), "yyyyMMddHHmmss");
		// 批次表使用 Date 类型展示分红周期，所以把 long 格式的周结束时间转回 Date。
		Date weekEndDate = DateUtil.parse(String.valueOf(weekEndTime), "yyyyMMddHHmmss");
		// 4. 先创建处理中批次，后续所有明细、钱包发放和奖池扣减都通过 batchNo 串起来追溯。
		batch.setPeriodStartTime(weekStartDate);
		batch.setPeriodEndTime(weekEndDate);
		batch.setPlanAmount(poolAmount);
		batch.setActualAmount(BigDecimal.ZERO);
		batch.setUserCount(0);
		batch.setStatus(GLOBAL_DIVIDEND_BATCH_PROCESSING);
		batch.setCreateTime(now);
		stakeHostingGlobalDividendBatchService.save(batch);

		// 5. 按等级分红比例和本周正数小区新增积分生成用户分红明细；这里只计算，不写钱包。
		List<StakeHostingGlobalDividendDetail> details = buildGlobalDividendDetails(batchNo, poolAmount, weekStartTime);
		BigDecimal actualAmount = BigDecimal.ZERO;
		for (StakeHostingGlobalDividendDetail detail : details) {
			actualAmount = actualAmount.add(detail.getRewardAmount());
		}
		actualAmount = actualAmount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if (actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("托管全球分红：无满足小区业绩权重的用户，batchNo={}", batchNo);
			// 6. 有奖池但没有可分用户时，批次结束为0发放，不扣奖池，不写分红明细。
			finishGlobalDividendBatch(batch.getId(), actualAmount, 0, now);
			addWeeklyTask(strDate);
			return;
		}

		// 7. 保存分红明细快照，包含等级分红比例、等级奖池、用户权重和实际奖励金额。
		stakeHostingGlobalDividendDetailService.saveBatch(details);
		// 8. 逐笔发放用户USDT全球分红：增加钱包余额并写RewardRecord。
		for (StakeHostingGlobalDividendDetail detail : details) {
			grantGlobalDividend(batchNo, detail, now);
		}
		// 9. 按实际发放金额扣减全球分红奖池，并写奖池支出流水。
		stakeHostingGlobalDividendPoolService.expenseWeeklyDividend(batchNo, actualAmount, "task102");
		// 10. 标记本周参与分红的周业绩记录，写入批次号，避免后续排查不知道哪周记录被哪个批次使用。
		markWeeklyPerformanceSettled(batchNo, weekStartTime, details, now);
		// 11. 更新批次为已完成，并在最后写102任务记录，避免中途失败却被标记为已执行。
		finishGlobalDividendBatch(batch.getId(), actualAmount, details.size(), now);
		addWeeklyTask(strDate);
	}

	/**
	 * 测试计算托管静态日利率。
	 *
	 * <p>本方法复用101任务的G7快照准备和上下文预加载，但只输出每笔产出中订单命中的基础静态日利率。
	 * 它不会发放奖励、不会写钱包、不会修改订单收益字段；并且会绕开 `FORCE_TEST_STATIC_RATE`，
	 * 用于单独核对真实G7/指定收益率/未推广规则是否正确。</p>
	 *
	 * @param rewardDay 收益日期，格式yyyyMMdd；为空时默认当天
	 * @return 每笔产出中托管订单的基础静态日利率测试结果
	 */
	@Override
	public List<StakeHostingStaticRateTestDto> testCalculateStaticRate(Integer rewardDay) {
		int statDay = rewardDay == null ? Integer.parseInt(DateUtil.format(DateUtil.date(), "yyyyMMdd")) : rewardDay;
		List<StakeHostingOrder> orderList = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.list();
		if (CollectionUtil.isEmpty(orderList)) {
			log.info("托管静态日利率测试：无产出中托管订单，rewardDay={}", statDay);
			return new ArrayList<>();
		}
		List<Long> rewardUserIds = orderList.stream()
			.map(StakeHostingOrder::getUserId)
			.distinct()
			.collect(Collectors.toList());
		// 与101任务保持一致：先生成/补齐当天G7快照，再基于快照判断订单基础日利率。
		stakeHostingDailyTeamPerformanceService.prepareDailySnapshots(statDay, rewardUserIds);
		StaticRewardCalculateContext context = buildStaticRewardCalculateContext(orderList, statDay);
		List<StakeHostingStaticRateTestDto> results = new ArrayList<>(orderList.size());
		for (StakeHostingOrder order : orderList) {
			StakeHostingStaticRateTestDto result = calculateStaticRateForTest(order, statDay, context);
			results.add(result);
			log.info("托管静态日利率测试：rewardDay={}, orderNo={}, userId={}, source={}, finalRate={}%, gDay={}, gSmooth={}, remark={}",
				statDay, result.getOrderNo(), result.getUserId(), result.getRateSource(), result.getFinalStaticRate(),
				result.getGDay(), result.getGSmooth(), result.getRemark());
		}
		return results;
	}

	private void addWeeklyTask(String strDate) {
		int rows = asyncTaskServiceImpl.addTask(SysConstant.TSK_TYPE_102, strDate);
		if (rows != 1) {
			throw new RuntimeException("任务类型102 每周托管全球分红任务插入失败");
		}
	}

	/**
	 * 计算本周全球分红明细。
	 *
	 * <p>本方法只负责计算并构造明细快照，不写钱包、不扣奖池。计算口径为：
	 * 先按 `t_user_level_config.global_fee_dividend_ratio` 把本周可分奖池切成等级奖池；
	 * 再在同等级内，只取 `community_new_performance > 0` 且仍为有效用户的成员，
	 * 按用户本周正数新增小区业绩占该等级总正数新增小区业绩的比例分配等级奖池。</p>
	 *
	 * @param batchNo 本次全球分红批次号
	 * @param poolAmount 本次计划用于分红的奖池金额，单位USDT
	 * @param weekStartTime 本次分红对应自然周开始时间，格式yyyyMMddHHmmss
	 * @return 本次应生成的全球分红明细列表；无可分用户时返回空列表
	 */
	private List<StakeHostingGlobalDividendDetail> buildGlobalDividendDetails(String batchNo, BigDecimal poolAmount, Long weekStartTime) {
		// 1. 读取配置了全球分红占比的F等级；没有等级占比就无法切分等级奖池。
		List<UserLevelConfig> configs = userLevelConfigService.lambdaQuery()
			.gt(UserLevelConfig::getLevel, 0)
			.gt(UserLevelConfig::getGlobalFeeDividendRatio, BigDecimal.ZERO)
			.list();
		if (CollectionUtil.isEmpty(configs)) {
			return new ArrayList<>();
		}
		// 2. 读取本周正数新增小区业绩记录；0和负数只保留记录，不参与全球分红分母，也不生成明细。
		List<StakeHostingWeeklyCommunityPerformance> performances = stakeHostingWeeklyCommunityPerformanceService.lambdaQuery()
			.eq(StakeHostingWeeklyCommunityPerformance::getWeekStartTime, weekStartTime)
			.eq(StakeHostingWeeklyCommunityPerformance::getDeleted, 0)
			.gt(StakeHostingWeeklyCommunityPerformance::getCommunityNewPerformance, BigDecimal.ZERO)
			.list();
		if (CollectionUtil.isEmpty(performances)) {
			return new ArrayList<>();
		}
		// 3. 转成 userId -> 周业绩快照，后续按用户快速取本周正数新增小区业绩。
		Map<Long, StakeHostingWeeklyCommunityPerformance> performanceMap = performances.stream()
			.collect(Collectors.toMap(StakeHostingWeeklyCommunityPerformance::getUserId, Function.identity(), (a, b) -> a));
		// 4. 只允许当前有效用户参与全球分红；有效用户口径为持有未完成托管订单。
		List<UserInfo> users = userInfoService.lambdaQuery()
			.eq(UserInfo::getIsValid, 1)
			.in(UserInfo::getUserId, new ArrayList<>(performanceMap.keySet()))
			.list();
		if (CollectionUtil.isEmpty(users)) {
			return new ArrayList<>();
		}
		// 5. 按用户最终生效F等级分组；同等级用户只在本等级奖池内按权重分配。
		Map<Integer, List<UserInfo>> userMap = users.stream()
			.filter(user -> effectiveLevel(user) > 0)
			.collect(Collectors.groupingBy(this::effectiveLevel));
		List<StakeHostingGlobalDividendDetail> details = new ArrayList<>();
		for (UserLevelConfig config : configs) {
			// 6. 当前等级没有符合条件的用户时，跳过该等级奖池。
			List<UserInfo> levelUsers = userMap.get(config.getLevel());
			if (CollectionUtil.isEmpty(levelUsers)) {
				continue;
			}
			// 7. 等级奖池 = 本次奖池总额 × 等级全球分红比例。
			BigDecimal levelPool = poolAmount.multiply(config.getGlobalFeeDividendRatio())
				.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			if (levelPool.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			// 8. 当前等级分母 = 本等级所有有效用户的正数周新增小区业绩之和。
			BigDecimal levelPerformance = levelUsers.stream()
				.map(user -> weeklyCommunityPerformance(performanceMap, user.getUserId()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			if (levelPerformance.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			for (UserInfo user : levelUsers) {
				// 9. 用户分红 = 等级奖池 × 用户周新增小区业绩 / 本等级周新增小区业绩总和。
				BigDecimal userPerformance = weeklyCommunityPerformance(performanceMap, user.getUserId());
				BigDecimal rewardAmount = levelPool.multiply(userPerformance)
					.divide(levelPerformance, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
				if (rewardAmount.compareTo(BigDecimal.ZERO) <= 0) {
					continue;
				}
				// 10. 明细保存本次计算快照，后续等级比例或用户业绩变化不影响历史分红记录。
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
	 * <p>2. 构造静态收益结算明细，收益钱包入账和奖励记录由调用方统一批量落库。</p>
	 * <p>3. 累加订单已发收益和运行天数，更新最近发放日期。</p>
	 * <p>4. 判断是否回本、是否达到套餐天数。</p>
	 * <p>5. 订单发满后仅标记为已完成；本金退还、托管业绩扣减、AFI退还和等级重算放到本轮奖励全部完成后统一处理。</p>
	 *
	 * @param order 本次待发放的产出中托管订单
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param now 本次任务执行时间
	 * @return 本订单本次静态收益计算结果，用于批量入账和后续团队奖励发放
	 */
	private StaticRewardResult distributeOne(StakeHostingOrder order, int rewardDay, Date now, StaticRewardCalculateContext context) {
		// 先确定本订单当天基础静态收益率；当前测试阶段可能被硬编码为1%，正式逻辑会走用户指定收益率/G7快照。
		BigDecimal todayRate = calculateStaticRate(order, rewardDay, context);
		// 基础静态毛收益：托管本金 × 基础静态收益率，未扣服务费、未做用户到账。
		BigDecimal baseGrossReward = order.getStakeUsdtAmount().multiply(todayRate)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 查询该订单当天已经生效且未退还的AFI质押记录，AFI倍率使用质押当时保存的历史快照。
		StakeHostingAfiPledge effectiveAfiPledge = getEffectiveAfiPledge(order.getId(), context);
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
		// 返回本订单完整静态收益计算结果，调用方统一批量入账静态收益并继续发放团队奖励。
		return new StaticRewardResult(order, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, reward, staticSettlement, finished);
	}

	/**
	 * 统一处理本轮101完成订单的到期后置动作。
	 *
	 * <p>到期后置动作必须放在静态收益、直推、极差、平级奖励都处理完之后。
	 * 这样同一用户同时存在1天和30天订单时，1天订单完成不会提前扣减业绩、失效用户或触发降级，
	 * 从而影响同批次其它订单的奖励判断。</p>
	 *
	 * @param results 本轮101任务已计算完成的静态收益结果
	 * @param now 本次任务执行时间
	 */
	private void handleFinishedOrdersAfterRewards(List<StaticRewardResult> results, Date now) {
		if (CollectionUtil.isEmpty(results)) {
			return;
		}
		List<StaticRewardResult> finishedResults = results.stream()
			.filter(result -> result.finished)
			.collect(Collectors.toList());
		if (CollectionUtil.isEmpty(finishedResults)) {
			return;
		}

		// 用户购买订单到期后需要退还USDT本金；后台拨付订单不是用户支付，不生成本金退还流水。
		refundFinishedUserPrincipal(finishedResults, now);

		Map<Long, Long> finishedUserOrderMap = new HashMap<>();
		for (StaticRewardResult result : finishedResults) {
			StakeHostingOrder order = result.order;
			// 奖励全部发放完成后再扣减托管业绩，保持本批次奖励资格读取的是订单完成前的口径。
			stakeHostingOrderService.subtractHostingPerformance(order.getUserId(), order.getStakeUsdtAmount(), order.getId());
			// AFI加速质押退还沿用原有服务能力；本次只调整USDT本金退还，不改变AFI退还逻辑。
			stakeHostingAfiPledgeService.returnPledgeByOrderId(order.getId());
			// G7静态日利率按每日新增对比计算，订单到期不再写入G7扣减；这里只收集用户用于刷新有效状态和等级。
			finishedUserOrderMap.putIfAbsent(order.getUserId(), order.getId());
		}
		for (Map.Entry<Long, Long> entry : finishedUserOrderMap.entrySet()) {
			stakeHostingOrderService.refreshUserValidByUnfinishedHostingOrder(entry.getKey());
			stakeHostingOrderService.sendStakeHostingLevelRecalculateAfterCommit(entry.getValue());
		}
	}

	/**
	 * 批量退还本轮到期用户购买订单的USDT本金。
	 *
	 * <p>本金退还不是收益奖励，所以只增加用户USDT钱包并写钱包流水，不写 RewardRecord。
	 * 只有 `source_type=0` 的用户购买订单才退还本金；`source_type=1` 的后台拨付订单不退还。</p>
	 *
	 * @param finishedResults 本轮101任务中已经达到套餐天数并完成的订单结果
	 * @param now 本次任务执行时间
	 */
	private void refundFinishedUserPrincipal(List<StaticRewardResult> finishedResults, Date now) {
		if (CollectionUtil.isEmpty(finishedResults)) {
			return;
		}
		int batchSize = 1000;
		List<UserMoney> userMoneyList = new ArrayList<>(Math.min(finishedResults.size(), batchSize));
		for (StaticRewardResult result : finishedResults) {
			StakeHostingOrder order = result.order;
			// 只退还用户购买订单本金；后台拨付订单没有真实USDT支付动作，不能生成“质押退还”流水。
			if (order.getSourceType() == null || order.getSourceType() != StakeHostingOrderServiceImpl.SOURCE_USER) {
				continue;
			}
			// 金额异常时跳过，避免写入0或负数本金退还流水。
			if (order.getStakeUsdtAmount() == null || order.getStakeUsdtAmount().compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			UserMoney userMoney = new UserMoney();
			userMoney.setId(order.getUserId());
			userMoney.setValidNum1(order.getStakeUsdtAmount().setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
			userMoney.setGtId(IDUtils.getSnowflakeStr());
			userMoney.setSourceCode(order.getOrderNo());
			userMoney.setSourceId(order.getUserId());
			userMoney.setSourceType(ConstantType.user_money_log_source_type.type_39);
			userMoney.setUpdateTime(now);
			userMoneyList.add(userMoney);
			if (userMoneyList.size() >= batchSize) {
				batchUpdateMoneyValid1(userMoneyList);
				userMoneyList.clear();
			}
		}
		batchUpdateMoneyValid1(userMoneyList);
		userMoneyList.clear();
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
			// 101 静态收益统一发 USDT，直接走 valid_num1 的 JDBC batch 增量入账，保持钱包流水字段可追溯。
			batchUpdateMoneyValid1(userMoneyList);
			userMoneyList.clear();
		}
		if (CollectionUtil.isNotEmpty(rewardRecordList)) {
			rewardRecordService.saveBatch(rewardRecordList);
			rewardRecordList.clear();
		}
	}

	/**
	 * 构建本轮101静态收益计算上下文。
	 *
	 * <p>静态收益发放是订单循环热路径，同一个用户可能有多笔产出中订单。这里提前批量加载用户指定收益率、
	 * 当天G7快照、纯静态收益率参数和当天可生效AFI质押记录，避免在每笔订单里重复查询数据库。</p>
	 *
	 * @param orderList 本轮101待发放的产出中托管订单
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @return 本轮静态收益计算上下文
	 */
	private StaticRewardCalculateContext buildStaticRewardCalculateContext(List<StakeHostingOrder> orderList, int rewardDay) {
		StaticRewardCalculateContext context = new StaticRewardCalculateContext();
		if (CollectionUtil.isEmpty(orderList)) {
			return context;
		}
		List<Long> userIds = orderList.stream()
			.map(StakeHostingOrder::getUserId)
			.distinct()
			.collect(Collectors.toList());
		List<Long> orderIds = orderList.stream()
			.map(StakeHostingOrder::getId)
			.distinct()
			.collect(Collectors.toList());

		// 纯静态收益率是本轮任务固定参数，在订单循环外读取一次即可。
		context.pureStaticRateBeforeReturnPercent = new BigDecimal(sysParaServiceImpl.getValue(ConstantSys.PURE_STATIC_RATE_BEFORE_RETURN_PERCENT));
		context.pureStaticRateAfterReturnPercent = new BigDecimal(sysParaServiceImpl.getValue(ConstantSys.PURE_STATIC_RATE_AFTER_RETURN_PERCENT));

		// 用户指定静态收益率按用户批量读取，同用户多订单直接复用。
		List<UserInfo> users = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, userIds)
			.list();
		if (CollectionUtil.isNotEmpty(users)) {
			context.userMap = users.stream()
				.collect(Collectors.toMap(UserInfo::getUserId, Function.identity(), (a, b) -> a));
		}

		// prepareDailySnapshots 已经生成当天G7快照，这里一次性查出，避免 calculateStaticRate 中重复查 hasTeamTvl/getCalculatedSnapshot。
		List<StakeHostingDailyTeamPerformance> snapshots = stakeHostingDailyTeamPerformanceService.lambdaQuery()
			.in(StakeHostingDailyTeamPerformance::getUserId, userIds)
			.eq(StakeHostingDailyTeamPerformance::getStatDay, rewardDay)
			.eq(StakeHostingDailyTeamPerformance::getCalcStatus, G7_CALC_STATUS_DONE)
			.eq(StakeHostingDailyTeamPerformance::getDeleted, DELETED_NO)
			.list();
		if (CollectionUtil.isNotEmpty(snapshots)) {
			context.snapshotMap = snapshots.stream()
				.collect(Collectors.toMap(StakeHostingDailyTeamPerformance::getUserId, Function.identity(), (a, b) -> a));
		}

		// 一个托管订单最多一笔AFI加速质押，按订单ID批量预加载当天可参与加速的记录。
		List<StakeHostingAfiPledge> pledges = stakeHostingAfiPledgeService.lambdaQuery()
			.in(StakeHostingAfiPledge::getStakeHostingOrderId, orderIds)
			.le(StakeHostingAfiPledge::getEffectiveDay, rewardDay)
			.eq(StakeHostingAfiPledge::getStatus, StakeHostingAfiPledgeServiceImpl.STATUS_EFFECTIVE)
			.list();
		if (CollectionUtil.isNotEmpty(pledges)) {
			context.afiPledgeMap = pledges.stream()
				.collect(Collectors.toMap(StakeHostingAfiPledge::getStakeHostingOrderId, Function.identity(), (a, b) -> a));
		}
		return context;
	}

	/**
	 * 本轮101静态收益计算上下文。
	 *
	 * <p>用于缓存订单循环热路径中会重复使用的数据：用户指定收益率、G7收益率快照、纯静态参数和AFI质押加速记录。</p>
	 */
	private static class StaticRewardCalculateContext {
		private Map<Long, UserInfo> userMap = new HashMap<>();
		private Map<Long, StakeHostingDailyTeamPerformance> snapshotMap = new HashMap<>();
		private Map<Long, StakeHostingAfiPledge> afiPledgeMap = new HashMap<>();
		private BigDecimal pureStaticRateBeforeReturnPercent = BigDecimal.ZERO;
		private BigDecimal pureStaticRateAfterReturnPercent = BigDecimal.ZERO;
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
		private final boolean finished;

		private StaticRewardResult(StakeHostingOrder order, BigDecimal grossReward, BigDecimal baseStaticRate,
								   BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
								   BigDecimal serviceFeeRatio, BigDecimal serviceFee, BigDecimal netReward,
								   StakeHostingRewardSettlement staticSettlement, boolean finished) {
			this.order = order;
			this.grossReward = grossReward;
			this.baseStaticRate = baseStaticRate;
			this.afiAccelerateRate = afiAccelerateRate;
			this.actualStaticRate = actualStaticRate;
			this.serviceFeeRatio = serviceFeeRatio;
			this.serviceFee = serviceFee;
			this.netReward = netReward;
			this.staticSettlement = staticSettlement;
			this.finished = finished;
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
	private StakeHostingAfiPledge getEffectiveAfiPledge(Long orderId, StaticRewardCalculateContext context) {
		if (orderId == null) {
			return null;
		}
		// AFI质押记录已在本轮101任务开始时按订单ID批量预加载，这里只做内存读取。
		return context.afiPledgeMap.get(orderId);
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
	 * 正式收益率优先级：用户指定收益率 > 未推广特殊规则 > G7团队新增业绩快照。
	 * 配置和快照里的收益率单位是%，本方法返回实际乘数，例如 0.5% 返回 0.005。</p>
	 *
	 * @param order 托管订单
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @return 基础静态收益率乘数
	 */
	private BigDecimal calculateStaticRate(StakeHostingOrder order, int rewardDay, StaticRewardCalculateContext context) {
		// 临时测试代码：所有托管订单静态日利率固定为1%，测完恢复正式收益率逻辑。
//		if (FORCE_TEST_STATIC_RATE) {
//			return percentToRate(TEST_STATIC_RATE_PERCENT);
//		}

		UserInfo user = context.userMap.get(order.getUserId());
		if (user != null && user.getStakeHostingStaticRate() != null && user.getStakeHostingStaticRate().compareTo(BigDecimal.ZERO) > 0) {
			return percentToRate(user.getStakeHostingStaticRate());
		}
		StakeHostingDailyTeamPerformance snapshot = context.snapshotMap.get(order.getUserId());
		if (snapshot == null) {
			BigDecimal pureStaticRate = loadPureStaticRatePercent(context, order.getIsReturnPrincipal() != null && order.getIsReturnPrincipal() == 1);
			return percentToRate(pureStaticRate);
		}
		if (snapshot.getBaseStaticRate() == null) {
			return PLACEHOLDER_STATIC_RATE;
		}
		return percentToRate(snapshot.getBaseStaticRate());
	}

	/**
	 * 按正式优先级计算单笔订单基础静态日利率测试结果。
	 *
	 * <p>该方法刻意不读取 `FORCE_TEST_STATIC_RATE`，用于排查真实日利率公式。返回值中的收益率单位均为百分比，
	 * 例如 0.7500 表示 0.75%。</p>
	 *
	 * @param order 托管订单
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param context 本轮测试预加载上下文
	 * @return 静态日利率测试结果
	 */
	private StakeHostingStaticRateTestDto calculateStaticRateForTest(StakeHostingOrder order, int rewardDay,
																	 StaticRewardCalculateContext context) {
		UserInfo user = context.userMap.get(order.getUserId());
		StakeHostingDailyTeamPerformance snapshot = context.snapshotMap.get(order.getUserId());
		BigDecimal finalStaticRate;
		String rateSource;
		String remark;
		if (user != null && user.getStakeHostingStaticRate() != null
			&& user.getStakeHostingStaticRate().compareTo(BigDecimal.ZERO) > 0) {
			finalStaticRate = user.getStakeHostingStaticRate();
			rateSource = "指定收益率";
			remark = "用户 stake_hosting_static_rate > 0，优先使用后台指定收益率";
		} else if (snapshot == null) {
			finalStaticRate = loadPureStaticRatePercent(context, order.getIsReturnPrincipal() != null && order.getIsReturnPrincipal() == 1);
			rateSource = "未推广规则";
			remark = order.getIsReturnPrincipal() != null && order.getIsReturnPrincipal() == 1
				? "昨日和今日团队新增均为0，订单已回本，使用回本后纯静态收益率"
				: "昨日和今日团队新增均为0，订单未回本，使用回本前纯静态收益率";
		} else if (snapshot.getBaseStaticRate() == null) {
			finalStaticRate = rateToPercent(PLACEHOLDER_STATIC_RATE);
			rateSource = "快照缺失兜底";
			remark = "G7快照存在但 base_static_rate 为空，使用0.5%兜底收益率";
		} else {
			finalStaticRate = snapshot.getBaseStaticRate();
			rateSource = "G7快照";
			remark = "使用当天G7快照 base_static_rate";
		}
		return StakeHostingStaticRateTestDto.builder()
			.orderId(order.getId())
			.orderNo(order.getOrderNo())
			.userId(order.getUserId())
			.stakeUsdtAmount(order.getStakeUsdtAmount())
			.stakeHostingStaticRate(user == null ? null : user.getStakeHostingStaticRate())
			.previousTeamTvl(snapshot == null ? null : snapshot.getPreviousTeamTvl())
			.currentTeamTvl(snapshot == null ? null : snapshot.getCurrentTeamTvl())
			.teamNewAmount(snapshot == null ? null : snapshot.getTeamNewAmount())
			.teamExpiredAmount(snapshot == null ? null : snapshot.getTeamExpiredAmount())
			.gDay(snapshot == null ? null : snapshot.getGDay())
			.gSmooth(snapshot == null ? null : snapshot.getGSmooth())
			.baseStaticRate(snapshot == null ? null : snapshot.getBaseStaticRate())
			.finalStaticRate(finalStaticRate == null ? null : finalStaticRate.setScale(4, ConstantStatic.roundingModeNew))
			.rateSource(rateSource)
			.remark(remark)
			.build();
	}

	/**
	 * Reads the pure static rate percent used when no G7 snapshot exists.
	 *
	 * @param returnedPrincipal true when the stake hosting order has returned principal
	 * @return percent value from t_sys_para, e.g. 0.5 means 0.5%
	 */
	private BigDecimal loadPureStaticRatePercent(StaticRewardCalculateContext context, boolean returnedPrincipal) {
		return returnedPrincipal
			? context.pureStaticRateAfterReturnPercent
			: context.pureStaticRateBeforeReturnPercent;
	}

	/**
	 * Converts a percent rate to a multiplier used for reward calculation.
	 *
	 * @param percentRate percent value, e.g. 0.5 means 0.5%
	 * @return multiplier value, e.g. 0.005
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
		List<ParentUserTaskVo> parentUsers = getCachedParentUsers(order.getUserId(), context);
		if (CollectionUtil.isEmpty(parentUsers)) {
			log.info("托管团队奖励跳过：源订单无上级，orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
			return;
		}
		distributeDirectReward(order, parentUsers.get(0), grossReward, baseStaticRate, afiAccelerateRate,
			actualStaticRate, serviceFeeRatio, serviceFee, netReward, rewardDay, now, context);
		List<ParentUserTaskVo> rewardParentUsers = getCachedRewardParentUsers(order.getUserId(), context);
		if (CollectionUtil.isEmpty(rewardParentUsers)) {
			log.info("托管极差/平级奖励跳过：源订单无持有未出局托管订单的上级，orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
			return;
		}
		distributeDiffAndSameLevelReward(order, rewardParentUsers, grossReward, baseStaticRate, afiAccelerateRate,
			actualStaticRate, serviceFeeRatio, serviceFee, netReward, rewardDay, now, context);
	}

	/**
	 * 发放直属上级直推奖，若直属上级未持有未出局托管订单则只写未到账结算明细。
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
		BigDecimal directRatioPercent = getCachedDirectRewardRatioPercent(context);
		BigDecimal directReward = calculateReward(netReward, directRatioPercent);
		if (directUser == null) {
			// 没有直属上级时，直推链路直接结束；按当前口径不写平台沉淀或未到账明细。
			log.info("托管直推奖励跳过：源订单无直属上级，orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
			return;
		}
		Integer skipReason = getRewardSkipReason(directUser);
		if (skipReason != null) {
			// 直属上级存在但 is_valid=0 时，只写未到账结算明细，保留本应获得的直推金额和跳过原因。
//			collectSkippedSettlement(context, order, directUser.getUserId(), REWARD_TYPE_PLATFORM, effectiveLevel(directUser), netReward, directRatioPercent,
//				directReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
//				serviceFeeRatio, serviceFee, netReward, ARRIVAL_NO, skipReason, rewardDay, now);
			return;
		}
		// 直属上级 is_valid=1，先收集直推奖钱包、RewardRecord和到账结算明细，稍后统一批量落库。
		collectTeamReward(context, order, directUser.getUserId(), REWARD_TYPE_DIRECT, effectiveLevel(directUser), netReward, directRatioPercent,
			directReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, netReward, rewardDay, now);
	}

	/**
	 * 按上级链路发放极差奖和平级奖。
	 *
	 * <p>极差和平级拆开计算：当前等级第一个有效用户先按“当前等级比例 - 已覆盖比例”拿完整极差；
	 * 若该等级为F5及以上，再以这笔极差金额作为平级池，向上穿透低等级寻找同级，遇到更高等级时结束本组。
	 * 调用方传入的上级链路已经过滤掉未持有未出局托管订单的用户，因此这类用户不占极差/平级份额。</p>
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
		// 读取F等级对应的团队奖励比例；本轮101任务内复用同一份配置，避免每笔订单重复查等级配置表。
		// Same-level rule: later same-level users cause the first user to yield half of this diff pool.
		Map<Integer, BigDecimal> levelRatioMap = getCachedLevelRatioMap(context);
		// 已覆盖比例只由“实际到账”的上级推进；未持有未出局托管订单的上级在进入本方法前已被过滤。
		BigDecimal coveredRatio = BigDecimal.ZERO;
		for (int i = 0; i < parentUsers.size(); i++) {
			ParentUserTaskVo parent = parentUsers.get(i);
			Integer level = effectiveLevel(parent);
			BigDecimal levelRatio = levelRatioMap.getOrDefault(level, BigDecimal.ZERO);
			if (levelRatio.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			// 当前等级比例减去已覆盖比例，就是本轮可发放的极差比例；没有差额则继续向上找更高等级。
			BigDecimal diffRatio = levelRatio.subtract(coveredRatio);
			if (diffRatio.compareTo(BigDecimal.ZERO) > 0) {
				// F5及以上先收集“穿透低等级、遇高等级终止”的同级组；F5以下只处理当前用户的极差。
				SameLevelGroup sameLevelGroup = level >= 5
					? collectSameLevelGroupUntilHigher(parentUsers, i, level)
					: SameLevelGroup.single(i);
				BigDecimal diffRewardAmount = calculateReward(netReward, diffRatio);
				if (CollectionUtil.isNotEmpty(sameLevelGroup.sameIndexes)) {
					ParentUserTaskVo diffUser = parentUsers.get(sameLevelGroup.sameIndexes.get(0));
					List<Integer> rewardSameIndexes = sameLevelGroup.rewardSameIndexes();
					boolean hasLaterSameLevel = CollectionUtil.isNotEmpty(rewardSameIndexes);
					// When later same-level users exist, the first user keeps half of this diff pool and yields the other half.
					BigDecimal firstDiffRewardAmount = hasLaterSameLevel
						? diffRewardAmount.divide(TWO, ConstantStatic.newScale, ConstantStatic.roundingModeNew)
						: diffRewardAmount;
					// 当前等级组的第一个同级用户在存在后续同级时只拿一半极差，否则仍拿完整极差。
					collectTeamReward(context, order, diffUser.getUserId(), REWARD_TYPE_DIFF, level, netReward, diffRatio,
						firstDiffRewardAmount, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
						serviceFeeRatio, serviceFee, netReward, rewardDay, now);
					// 只有用户实际拿到极差后，当前等级比例才算被覆盖，后续更高等级只拿补差。
					coveredRatio = levelRatio;
					if (level >= 5 && hasLaterSameLevel) {
						BigDecimal sameLevelPool = diffRewardAmount.subtract(firstDiffRewardAmount);
						// F5及以上：以后续同级让出的另一半极差金额作为平级池，按实际人数重新拆分。
						collectSameLevelReward(order, parentUsers, rewardSameIndexes,
							level, netReward, diffRatio, sameLevelPool, grossReward, baseStaticRate,
							afiAccelerateRate, actualStaticRate, serviceFeeRatio, serviceFee, rewardDay, now, context);
					}
				}
				// 本组已处理完：低等级已被穿透跳过；若遇到更高等级，则下一轮从更高等级继续拿补差。
				i = sameLevelGroup.nextIndex - 1;
			}
		}
	}

	/**
	 * 收集同级组的平级奖。
	 *
	 * <p>平级奖只在F5及以上触发，且以本段第一个到账同级用户已经拿到的极差金额作为平级池。
	 * 第一个同级用户只拿极差，不参与本组平级；后续同级按实际参与人数重新用 1/2、1/4、1/4 等公式拆完整个平级池。
	 * 没有未出局托管订单的用户已在进入本方法前过滤，不占份额。</p>
	 *
	 * @param order 源托管订单
	 * @param parentUsers 源用户上级链路，按近到远排序
	 * @param sameIndexes 本次平级组中参与平级的后续同级用户下标，顺序按近到远，不包含第一个拿极差的同级
	 * @param level 当前F等级
	 * @param netReward 用户到账静态净收益，单位USDT
	 * @param diffRatio 本段极差比例，单位%
	 * @param sameLevelPool 本段平级池金额，单位USDT，等于第一个有效同级用户拿到的极差金额
	 * @param grossReward AFI 加速后的静态毛收益，单位USDT
	 * @param baseStaticRate 基础静态收益率快照，单位%
	 * @param afiAccelerateRate AFI 加速倍率快照
	 * @param actualStaticRate 实际静态收益率快照，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额，单位USDT
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param now 本次任务执行时间
	 * @param context 团队奖励批量收集上下文
	 */
	private void collectSameLevelReward(StakeHostingOrder order, List<ParentUserTaskVo> parentUsers,
										List<Integer> sameIndexes, Integer level, BigDecimal netReward,
										BigDecimal diffRatio, BigDecimal sameLevelPool,
										BigDecimal grossReward, BigDecimal baseStaticRate,
										BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
										BigDecimal serviceFeeRatio, BigDecimal serviceFee,
										int rewardDay, Date now, TeamRewardCollectContext context) {
		if (CollectionUtil.isEmpty(sameIndexes) || sameLevelPool.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		for (int sameIndex = 0; sameIndex < sameIndexes.size(); sameIndex++) {
			ParentUserTaskVo rewardUser = parentUsers.get(sameIndexes.get(sameIndex));
			BigDecimal sameLevelReward = calculateSameLevelReward(sameLevelPool, sameIndex + 1, sameIndexes.size());
			// 当前平级组只包含后续有效同级用户；第一个拿极差的同级和被穿透的低等级用户都不参与、不占份额。
			collectTeamReward(context, order, rewardUser.getUserId(), REWARD_TYPE_SAME_LEVEL, level, netReward, diffRatio,
				sameLevelReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
				serviceFeeRatio, serviceFee, netReward, rewardDay, now);
		}
	}

	/**
	 * 收集从指定位置开始的穿透式平级组。
	 *
	 * <p>本方法只用于F5及以上等级：从第一个拿极差的同级开始向上扫描，同等级加入平级组；
	 * 低等级跳过且不占份额；遇到更高等级立即停止，让外层循环继续按更高等级补差。</p>
	 *
	 * @param parentUsers 源用户上级链路，按近到远排序
	 * @param startIndex 起始下标
	 * @param level 当前F等级
	 * @return 平级组下标和下一轮外层循环应处理的位置
	 */
	private SameLevelGroup collectSameLevelGroupUntilHigher(List<ParentUserTaskVo> parentUsers, int startIndex, Integer level) {
		List<Integer> sameIndexes = new ArrayList<>();
		int nextIndex = parentUsers.size();
		for (int i = startIndex; i < parentUsers.size(); i++) {
			Integer currentLevel = effectiveLevel(parentUsers.get(i));
			if (currentLevel > level) {
				nextIndex = i;
				break;
			}
			if (level.equals(currentLevel)) {
				sameIndexes.add(i);
			}
		}
		return new SameLevelGroup(sameIndexes, nextIndex);
	}

	/**
	 * 按平级公式拆分平级池。
	 *
	 * <p>两个同级：1/2 + 1/2；三个同级：1/2 + 1/4 + 1/4；
	 * 四个同级：1/2 + 1/4 + 1/8 + 1/8；更多同级时最后一个承接剩余同份额。</p>
	 *
	 * @param pool 平级池金额，单位USDT
	 * @param sameIndex 同级段内第几个用户，从1开始
	 * @param sameCount 同级段总人数
	 * @return 当前用户分得的平级奖励金额，单位USDT
	 */
	private BigDecimal calculateSameLevelReward(BigDecimal pool, int sameIndex, int sameCount) {
		if (sameCount <= 0 || pool.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		if (sameCount == 1) {
			return pool;
		}
		int power = sameIndex == sameCount ? sameCount - 1 : sameIndex;
		BigDecimal divisor = new BigDecimal(2).pow(power);
		return pool.divide(divisor, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	/**
	 * 判断团队奖励接收人是否需要跳过到账。
	 *
	 * <p>当前统一口径：`t_user_info.is_valid=1` 表示用户持有未出局的质押托管订单。
	 * 该字段由下单生效和托管订单全部到期流程维护，101发奖时只读取上级链路中已经带出的 isValid，
	 * 不再实时查询托管订单表。</p>
	 *
	 * @param user 待判断的上级用户
	 * @return null表示可以到账；非null表示未到账原因
	 */
	private Integer getRewardSkipReason(ParentUserTaskVo user) {
		if (!isValidStakeHostingRewardUser(user)) {
			return SKIP_NO_ACTIVE_ORDER;
		}
		return null;
	}

	/**
	 * 获取源用户的上级链路，并在本轮101任务中复用查询结果。
	 *
	 * <p>同一个用户可能存在多笔产出中托管订单，如果每笔订单都重新查询上级链路，会造成重复数据库访问。
	 * 这里按源用户ID缓存近到远的上级列表，团队奖励计算仍然使用同一份链路数据，不改变奖励口径。</p>
	 *
	 * @param userId 源订单用户ID
	 * @param context 本轮101任务团队奖励收集上下文，内部持有上级链路缓存
	 * @return 源用户上级链路；没有上级时返回空集合或null
	 */
	private List<ParentUserTaskVo> getCachedParentUsers(Long userId, TeamRewardCollectContext context) {
		if (userId == null) {
			return new ArrayList<>();
		}
		return context.parentUserCache.computeIfAbsent(userId, userInfoService::getParentUserTaskVo);
	}

	/**
	 * 获取极差/平级奖励使用的有效上级链路。
	 *
	 * <p>原始上级链路仍通过 {@link #getCachedParentUsers(Long, TeamRewardCollectContext)} 保留给直推奖使用。
	 * 极差/平级奖励的业务口径是“未持有未出局托管订单的上级等同查询不到”，因此这里新增过滤后的缓存链路，
	 * 避免无效用户占用平级拆分位置或推进覆盖比例。</p>
	 *
	 * @param userId 源订单用户ID
	 * @param context 本轮101任务团队奖励收集上下文
	 * @return 只包含持有未出局托管订单上级的链路，仍保持近到远顺序
	 */
	private List<ParentUserTaskVo> getCachedRewardParentUsers(Long userId, TeamRewardCollectContext context) {
		if (userId == null) {
			return new ArrayList<>();
		}
		return context.rewardParentUserCache.computeIfAbsent(userId, item -> {
			List<ParentUserTaskVo> parentUsers = getCachedParentUsers(item, context);
			if (CollectionUtil.isEmpty(parentUsers)) {
				return new ArrayList<>();
			}
			return parentUsers.stream()
				.filter(this::isValidStakeHostingRewardUser)
				.collect(Collectors.toList());
		});
	}

	/**
	 * 判断上级用户是否具备托管团队奖励资格。
	 *
	 * <p>业务上由下单生效、订单全部到期流程维护 `t_user_info.is_valid`：
	 * 1 表示当前持有未出局的质押托管订单，0 或 null 表示没有。团队奖励只读取这个维护结果，
	 * 避免101任务发奖时重复扫描托管订单表。</p>
	 *
	 * @param user 上级用户链路信息
	 * @return true表示可以参与直推、极差、平级等奖励到账
	 */
	private boolean isValidStakeHostingRewardUser(ParentUserTaskVo user) {
		return user != null && user.getIsValid() != null && user.getIsValid() == 1;
	}

	/**
	 * 获取本轮101任务使用的F等级团队奖励比例配置。
	 *
	 * <p>等级奖励比例是团队奖励计算的公共配置，同一轮101任务中每笔订单都可以复用。
	 * 这里懒加载到团队奖励上下文，避免每个订单进入极差/平级计算时都查询一次等级配置表。</p>
	 *
	 * @param context 本轮101任务团队奖励收集上下文
	 * @return F等级到团队奖励比例的映射，比例单位为%
	 */
	private Map<Integer, BigDecimal> getCachedLevelRatioMap(TeamRewardCollectContext context) {
		if (context.levelRatioMap == null) {
			context.levelRatioMap = getLevelRatioMap();
		}
		return context.levelRatioMap;
	}

	/**
	 * 查询F等级团队奖励比例配置。
	 *
	 * <p>返回值只描述“等级可覆盖的团队奖励比例”，不负责判断用户是否有效、是否有托管订单。
	 * 调用方会用该比例和coveredRatio计算本轮极差池。</p>
	 *
	 * @return F等级到团队奖励比例的映射，比例单位为%
	 */
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

	/**
	 * 获取本轮101任务使用的直推奖励比例。
	 *
	 * <p>直推比例来自系统参数，同一轮101任务内不需要每笔订单都读取一次。
	 * 这里缓存在团队奖励上下文中，避免用户多订单时重复查询参数表。</p>
	 *
	 * @param context 本轮101任务团队奖励收集上下文
	 * @return 直推奖励比例，单位%
	 */
	private BigDecimal getCachedDirectRewardRatioPercent(TeamRewardCollectContext context) {
		if (context.directRewardRatioPercent == null) {
			context.directRewardRatioPercent = getDirectRewardRatioPercent();
		}
		return context.directRewardRatioPercent;
	}

	/**
	 * 查询托管直推奖励比例配置。
	 *
	 * <p>该方法只负责读取系统参数，调用方应在批量任务上下文中缓存结果，避免在订单循环热路径中重复查询。</p>
	 *
	 * @return 直推奖励比例，单位%
	 */
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
	 * <p>未持有未出局托管订单的上级不发钱包、不写RewardRecord，只保留未到账结算明细用于追溯。</p>
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
			// 直推、极差、平级奖励都发 USDT，和静态收益一样批量累加到用户 valid_num1。
			batchUpdateMoneyValid1(context.userMoneyList);
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
	 * 批量增加用户 USDT 钱包余额。
	 *
	 * <p>本方法专供 101 托管USDT批量入账使用，覆盖静态收益、团队奖励和用户购买订单到期本金退还。
	 * 能力对齐 AsyncTaskServiceImpl 中的
	 * bachUpdateMoneyValid1：每条记录按 userId 更新 t_user_money.valid_num1，并同步写入
	 * gtId/sourceCode/sourceType/sourceId/updateTime，方便钱包流水追溯资金来源。</p>
	 *
	 * @param userMoneyList 待入账的钱包增量记录，金额取 validNum1，币种为 USDT
	 */
	private void batchUpdateMoneyValid1(List<UserMoney> userMoneyList) {
		if (CollectionUtil.isEmpty(userMoneyList)) {
			return;
		}
		int[] rows = jdbcTemplate.batchUpdate(SQL_VALID_NUM1, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UserMoney userMoney = userMoneyList.get(i);
				ps.setTimestamp(1, new java.sql.Timestamp(userMoney.getUpdateTime().getTime()));
				ps.setString(2, userMoney.getGtId());
				ps.setBigDecimal(3, userMoney.getValidNum1());
				ps.setString(4, userMoney.getSourceCode());
				ps.setInt(5, userMoney.getSourceType());
				ps.setLong(6, userMoney.getSourceId());
				ps.setLong(7, userMoney.getId());
			}

			@Override
			public int getBatchSize() {
				return userMoneyList.size();
			}
		});
		if (ArrayUtil.contains(rows, 0)) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("101托管USDT批量更新钱包余额失败，存在未更新的钱包记录");
			throw new ServiceException("101托管USDT批量入账失败");
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
		private final Map<Long, List<ParentUserTaskVo>> parentUserCache = new HashMap<>();
		private final Map<Long, List<ParentUserTaskVo>> rewardParentUserCache = new HashMap<>();
		private Map<Integer, BigDecimal> levelRatioMap;
		private BigDecimal directRewardRatioPercent;

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
	 * 穿透式平级组扫描结果。
	 *
	 * <p>sameIndexes 只保存当前等级的有效同级用户下标；中间低等级用户会被跳过但不进入列表。
	 * nextIndex 表示扫描停止位置：如果遇到更高等级，则指向该更高等级下标；如果未遇到，则等于链路长度。</p>
	 */
	private static class SameLevelGroup {
		private final List<Integer> sameIndexes;
		private final int nextIndex;

		private SameLevelGroup(List<Integer> sameIndexes, int nextIndex) {
			this.sameIndexes = sameIndexes;
			this.nextIndex = nextIndex;
		}

		/**
		 * 返回真正参与平级池拆分的后续同级用户下标。
		 *
		 * <p>sameIndexes 的第一个用户已经拿到本等级极差，只作为平级池来源，不再参与本组平级分配。</p>
		 *
		 * @return 去掉第一个拿极差用户后的同级下标列表
		 */
		private List<Integer> rewardSameIndexes() {
			if (sameIndexes.size() <= 1) {
				return new ArrayList<>();
			}
			return new ArrayList<>(sameIndexes.subList(1, sameIndexes.size()));
		}

		/**
		 * 构造只包含当前用户的非平级组，用于F5以下只发极差的场景。
		 *
		 * @param index 当前处理用户在上级链路中的下标
		 * @return 单用户扫描结果，下一轮从当前用户后一位继续
		 */
		private static SameLevelGroup single(int index) {
			List<Integer> indexes = new ArrayList<>();
			indexes.add(index);
			return new SameLevelGroup(indexes, index + 1);
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
