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
import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.domain.StakeHostingRewardSettlement;
import com.xms.dao.domain.StakeHostingUserRewardSummary;
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
import com.xms.dao.service.IStakeHostingGlobalDividendWeightSnapshotService;
import com.xms.dao.service.ISysParaService;
import com.xms.dao.service.IUserLevelConfigService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import com.xms.dao.service.impl.StakeHostingAfiPledgeServiceImpl;
import com.xms.dao.service.impl.StakeHostingOrderServiceImpl;
import com.xms.dao.service.impl.StakeHostingGlobalDividendWeightSnapshotServiceImpl;
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
 *
 */
@Slf4j
@Service
@AllArgsConstructor
public class StakeHostingTaskServiceImpl implements IStakeHostingTaskService {
	/**
	 *
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
	private final IStakeHostingGlobalDividendWeightSnapshotService stakeHostingGlobalDividendWeightSnapshotService;
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
	private static final int GLOBAL_DIVIDEND_SNAPSHOT_NOT_SETTLED = 0;
	private static final int GLOBAL_DIVIDEND_SNAPSHOT_SETTLED = 1;
	private static final int G7_CALC_STATUS_DONE = 1;
	private static final int RATE_SOURCE_PURE_STATIC = 3;
	private static final int DELETED_NO = 0;

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void distributeDailyStaticReward() {
		String strDate = DateUtil.format(DateUtil.date(), "yyyyMMdd");
		int rewardDay = Integer.parseInt(strDate);
		// Business processing note.
		Map<String, Object> task = asyncTaskServiceImpl.getTask(SysConstant.TSK_TYPE_101, strDate);
		if (!CollectionUtil.isEmpty(task)) {
			log.debug("Task already exists");
			return;
		}

		// Business processing note.
		List<StakeHostingOrder> orderList = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			// Business processing note.
//			.and(wrapper -> wrapper.ne(StakeHostingOrder::getLastRewardDay, rewardDay).or().isNull(StakeHostingOrder::getLastRewardDay))
			.list();
		if (CollectionUtil.isEmpty(orderList)) {
			log.info("Business processing skipped");
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
			// Business processing note.
			StaticRewardResult result = distributeOne(order, rewardDay, now, staticContext);
			staticRewardResults.add(result);
			dailyServiceFee = dailyServiceFee.add(result.serviceFee);
		}
		// Business processing note.
		saveStaticRewardSettlements(staticRewardResults);
		// Business processing note.
		grantStaticRewards(staticRewardResults, now);
		TeamRewardCollectContext teamRewardContext = new TeamRewardCollectContext();
		for (StaticRewardResult result : staticRewardResults) {
			if (result.shouldDistributeTeamReward()) {
				// Business processing note.
				distributeTeamReward(result.order, result.grossReward, result.baseStaticRate, result.afiAccelerateRate,
					result.actualStaticRate, result.serviceFeeRatio, result.serviceFee, result.netReward, rewardDay, now,
					teamRewardContext);
			}
		}
		// Business processing note.
		flushTeamRewardContext(teamRewardContext);
		// Business processing note.
		handleFinishedOrdersAfterRewards(staticRewardResults, now);
		// Business processing note.
		stakeHostingGlobalDividendPoolService.incomeDailyServiceFee(rewardDay,
			dailyServiceFee.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew), "task101");
		// Business processing note.
		// Business processing note.
		//addDailyTask(strDate);
	}

	/**
	 *
	 */
	private void addDailyTask(String strDate) {
		int rows = asyncTaskServiceImpl.addTask(SysConstant.TSK_TYPE_101, strDate);
		if (rows != 1) {
			throw new RuntimeException("Add 101 daily task failed");
		}
	}


	/**
	 * 执行 102 每周全球分红结算。
	 *
	 * <p>本任务先按本周结算时刻的 `t_user_info` 当前全球分红权重生成用户级周快照，
	 * 再用本周小区权重减上一期小区权重得到本期分红权重。只有实际生成分红明细并完成发放的快照，
	 * 才会标记为已参与分红；未参与用户仍保留本周快照，供下一周继续计算差值。</p>
	 *
	 * <p>任务副作用包括：写入全球分红批次、快照、分红明细，给用户钱包发放 USDT，
	 * 记录 RewardRecord，扣减全球分红奖池，并写入 102 异步任务完成记录。</p>
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void distributeWeeklyGlobalDividend() {
		// 1. 使用当天 yyyyMMdd 做 102 幂等键，同一天已经生成任务记录则不重复结算。
		String strDate = DateUtil.format(DateUtil.date(), "yyyyMMdd");
		int settlementDay = Integer.parseInt(strDate);
		Map<String, Object> task = asyncTaskServiceImpl.getTask(SysConstant.TSK_TYPE_102, strDate);
		if (!CollectionUtil.isEmpty(task)) {
			log.debug("102 weekly task already exists");
			return;
		}

		// 2. 以昨天所在自然周作为结算周，避免周一凌晨运行时误算到新周。
		Date now = new Date();
		// 取“昨天”作为周归属参考点：例如周一执行 102 时，应结算刚结束的上一周，而不是周一所在的新周。
		Date weekReference = DateUtil.offsetDay(now, -1);
		// 将参考日期转成 yyyyMMddHHmmss 的 long 形式，后续周起止工具统一使用该格式计算。
		Long referenceTime = StakeHostingGlobalDividendWeightSnapshotServiceImpl.formatDate(weekReference);
		// 本期分红周开始时间，格式 yyyyMMddHHmmss，用于快照唯一键和查询上一期快照。
		Long weekStartTime = StakeHostingGlobalDividendWeightSnapshotServiceImpl.weekStartTimeOf(referenceTime);
		// 本期分红周结束时间，格式 yyyyMMddHHmmss，用于快照展示和批次周期记录。
		Long weekEndTime = StakeHostingGlobalDividendWeightSnapshotServiceImpl.weekEndTimeOf(referenceTime);
		// 批次表 period_start_time 使用 Date 类型，因此把 long 格式周开始时间转回 Date。
		Date weekStartDate = DateUtil.parse(String.valueOf(weekStartTime), "yyyyMMddHHmmss");
		// 批次表 period_end_time 使用 Date 类型，因此把 long 格式周结束时间转回 Date。
		Date weekEndDate = DateUtil.parse(String.valueOf(weekEndTime), "yyyyMMddHHmmss");

		// 3. 先生成全量用户周快照；即使奖池为 0 也要落库，否则下一周差值会被放大。
		prepareGlobalDividendWeightSnapshots(weekStartTime, weekEndTime, now);
		BigDecimal poolAmount = stakeHostingGlobalDividendPoolService.getOrInitPool().getBalanceAmount();
		if (poolAmount == null || poolAmount.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("102 weekly global dividend skipped because pool balance is zero, weekStartTime={}", weekStartTime);
			addWeeklyTask(strDate);
			return;
		}

		// 4. 创建分红批次，先记录计划发放金额，实际金额在明细计算和发放后回写。
		String batchNo = IDUtils.getSnowflakeStr();
		StakeHostingGlobalDividendBatch batch = new StakeHostingGlobalDividendBatch();
		batch.setBatchNo(batchNo);
		batch.setSettlementDay(settlementDay);
		batch.setPeriodStartTime(weekStartDate);
		batch.setPeriodEndTime(weekEndDate);
		batch.setPlanAmount(poolAmount);
		batch.setActualAmount(BigDecimal.ZERO);
		batch.setUserCount(0);
		batch.setStatus(GLOBAL_DIVIDEND_BATCH_PROCESSING);
		batch.setCreateTime(now);
		stakeHostingGlobalDividendBatchService.save(batch);

		// 5. 按本周快照的 dividend_weight 计算分红明细，同等级内按权重占比分配等级奖池。
		List<StakeHostingGlobalDividendDetail> details = buildGlobalDividendDetails(batchNo, poolAmount, weekStartTime);
		BigDecimal actualAmount = BigDecimal.ZERO;
		for (StakeHostingGlobalDividendDetail detail : details) {
			actualAmount = actualAmount.add(detail.getRewardAmount());
		}
		actualAmount = actualAmount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if (actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("102 weekly global dividend produced no payable detail, batchNo={}, weekStartTime={}", batchNo, weekStartTime);
			// 6. 无可发用户时仍结束批次并写入任务记录，但不扣减奖池，也不标记快照已参与。
			finishGlobalDividendBatch(batch.getId(), actualAmount, 0, now);
			addWeeklyTask(strDate);
			return;
		}

		// 7. 保存分红明细，明细字段 userCommunityPerformance/levelCommunityPerformance 兼容保存分红权重。
		stakeHostingGlobalDividendDetailService.saveBatch(details);
		// 8. 逐条发放用户 USDT 钱包并写入 RewardRecord，保持现有全球分红发放链路不变。
		for (StakeHostingGlobalDividendDetail detail : details) {
			grantGlobalDividend(batchNo, detail, now);
		}
		// 9. 按实际发放金额扣减全球分红奖池。
		stakeHostingGlobalDividendPoolService.expenseWeeklyDividend(batchNo, actualAmount, "task102");
		// 10. 仅将实际生成分红明细的用户快照标记为已参与，并写入本批次号。
		markWeightSnapshotSettled(batchNo, weekStartTime, details, now);
		// 11. 回写批次实际金额和人数，最后写入 102 完成记录，防止当天重复执行。
		finishGlobalDividendBatch(batch.getId(), actualAmount, details.size(), now);
		addWeeklyTask(strDate);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	@Override
	public List<StakeHostingStaticRateTestDto> testCalculateStaticRate(Integer rewardDay) {
		int statDay = rewardDay == null ? Integer.parseInt(DateUtil.format(DateUtil.date(), "yyyyMMdd")) : rewardDay;
		List<StakeHostingOrder> orderList = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.list();
		if (CollectionUtil.isEmpty(orderList)) {
			log.info("Business processing log");
			return new ArrayList<>();
		}
		List<Long> rewardUserIds = orderList.stream()
			.map(StakeHostingOrder::getUserId)
			.distinct()
			.collect(Collectors.toList());
		// Business processing note.
		stakeHostingDailyTeamPerformanceService.prepareDailySnapshots(statDay, rewardUserIds);
		StaticRewardCalculateContext context = buildStaticRewardCalculateContext(orderList, statDay);
		List<StakeHostingStaticRateTestDto> results = new ArrayList<>(orderList.size());
		for (StakeHostingOrder order : orderList) {
			StakeHostingStaticRateTestDto result = calculateStaticRateForTest(order, statDay, context);
			results.add(result);
			log.info("Static rate test result rewardDay={}, orderNo={}, userId={}, source={}, finalRate={}, gDay={}, gSmooth={}, remark={}",
				statDay, result.getOrderNo(), result.getUserId(), result.getRateSource(), result.getFinalStaticRate(),
				result.getGDay(), result.getGSmooth(), result.getRemark());
		}
		return results;
	}

	private void addWeeklyTask(String strDate) {
//		int rows = asyncTaskServiceImpl.addTask(SysConstant.TSK_TYPE_102, strDate);
//		if (rows != 1) {
//			throw new RuntimeException("Task processing failed");
//		}
	}

	/**
	 * 根据用户当前全球分红权重生成本周用户级快照。
	 *
	 * <p>每次 102 都要给所有未删除用户写入本周快照，即使用户本周小区权重为 0 或比上一期下降。
	 * 下一周会把本周快照作为上一期小区权重，如果跳过本周为 0/下降的用户，后续差值会被错误放大。</p>
	 *
	 * @param weekStartTime 分红周开始时间，格式 yyyyMMddHHmmss
	 * @param weekEndTime 分红周结束时间，格式 yyyyMMddHHmmss
	 * @param now 任务执行时间
	 */
	private void prepareGlobalDividendWeightSnapshots(Long weekStartTime, Long weekEndTime, Date now) {
		// 1. 批量读取所有未删除用户当前权重，避免按用户逐个查库。
		List<UserInfo> users = userInfoService.lambdaQuery()
			.eq(UserInfo::getDeleted, DELETED_NO)
			.list();
		if (CollectionUtil.isEmpty(users)) {
			return;
		}
		// 2. 批量读取每个用户本周之前最近一期快照，作为 previous_community_weight。
		Map<Long, BigDecimal> previousCommunityWeightMap = stakeHostingGlobalDividendWeightSnapshotService
			.selectLatestBeforeWeek(weekStartTime)
			.stream()
			.collect(Collectors.toMap(StakeHostingGlobalDividendWeightSnapshot::getUserId,
				snapshot -> nvl(snapshot.getCommunityWeight()), (a, b) -> a));
		List<StakeHostingGlobalDividendWeightSnapshot> snapshots = new ArrayList<>(users.size());
		for (UserInfo user : users) {
			// 3. 本期分红权重只取小区权重上涨部分，下降或持平都保存快照但 dividend_weight 记 0。
			BigDecimal communityWeight = nvl(user.getGlobalDividendCommunityWeight());
			BigDecimal previousCommunityWeight = previousCommunityWeightMap.getOrDefault(user.getUserId(), BigDecimal.ZERO);
			BigDecimal dividendWeight = communityWeight.subtract(previousCommunityWeight)
				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			if (dividendWeight.compareTo(BigDecimal.ZERO) < 0) {
				dividendWeight = BigDecimal.ZERO;
			}
			StakeHostingGlobalDividendWeightSnapshot snapshot = new StakeHostingGlobalDividendWeightSnapshot();
			snapshot.setUserId(user.getUserId());
			snapshot.setAccount(user.getAccount());
			snapshot.setWeekStartTime(weekStartTime);
			snapshot.setWeekEndTime(weekEndTime);
			snapshot.setSelfWeight(nvl(user.getGlobalDividendWeight()));
			snapshot.setUmbrellaWeight(nvl(user.getGlobalDividendUmbrellaWeight()));
			snapshot.setCommunityWeight(communityWeight);
			snapshot.setPreviousCommunityWeight(previousCommunityWeight);
			snapshot.setDividendWeight(dividendWeight);
			snapshot.setSettleStatus(GLOBAL_DIVIDEND_SNAPSHOT_NOT_SETTLED);
			snapshot.setBatchNo(null);
			snapshot.setCreateTime(now);
			snapshot.setUpdateTime(now);
			snapshots.add(snapshot);
		}
		// 4. 按 user_id + week_start_time 幂等批量写入，任务失败重跑不会重复生成同周快照。
		for (int i = 0; i < snapshots.size(); i += 1000) {
			int end = Math.min(i + 1000, snapshots.size());
			stakeHostingGlobalDividendWeightSnapshotService.batchUpsert(snapshots.subList(i, end));
		}
	}

	/**
	 * 根据本周快照分红权重计算全球分红明细。
	 *
	 * <p>只读取本周 `dividend_weight > 0` 的快照，再叠加有效用户和有效 F 等级条件。
	 * 明细表字段名保持兼容：userCommunityPerformance 保存用户本期分红权重，
	 * levelCommunityPerformance 保存同等级本期分红权重合计。</p>
	 *
	 * @param batchNo 全球分红批次号
	 * @param poolAmount 本批次计划分红池金额，单位 USDT
	 * @param weekStartTime 分红周开始时间，格式 yyyyMMddHHmmss
	 * @return 待保存和发放的分红明细
	 */
	private List<StakeHostingGlobalDividendDetail> buildGlobalDividendDetails(String batchNo, BigDecimal poolAmount, Long weekStartTime) {
		// 1. 读取开启全球分红比例的 F 等级配置，等级奖池按该比例从总奖池中切分。
		List<UserLevelConfig> configs = userLevelConfigService.lambdaQuery()
			.gt(UserLevelConfig::getLevel, 0)
			.gt(UserLevelConfig::getGlobalFeeDividendRatio, BigDecimal.ZERO)
			.list();
		if (CollectionUtil.isEmpty(configs)) {
			return new ArrayList<>();
		}
		// 2. 读取本周正向差值快照，dividend_weight <= 0 的用户本期不参与分红。
		List<StakeHostingGlobalDividendWeightSnapshot> snapshots = stakeHostingGlobalDividendWeightSnapshotService.lambdaQuery()
			.eq(StakeHostingGlobalDividendWeightSnapshot::getWeekStartTime, weekStartTime)
			.eq(StakeHostingGlobalDividendWeightSnapshot::getDeleted, DELETED_NO)
			.gt(StakeHostingGlobalDividendWeightSnapshot::getDividendWeight, BigDecimal.ZERO)
			.list();
		if (CollectionUtil.isEmpty(snapshots)) {
			return new ArrayList<>();
		}
		Map<Long, StakeHostingGlobalDividendWeightSnapshot> snapshotMap = snapshots.stream()
			.collect(Collectors.toMap(StakeHostingGlobalDividendWeightSnapshot::getUserId, snapshot -> snapshot, (a, b) -> a));
		// 3. 批量读取正向差值用户中的当前有效用户，未持有有效托管订单的用户保留快照但不分红。
		List<UserInfo> users = userInfoService.lambdaQuery()
			.eq(UserInfo::getIsValid, 1)
			.eq(UserInfo::getDeleted, DELETED_NO)
			.in(UserInfo::getUserId, new ArrayList<>(snapshotMap.keySet()))
			.list();
		if (CollectionUtil.isEmpty(users)) {
			return new ArrayList<>();
		}
		// 4. 按有效 F 等级分组；effectiveLevel 会过滤掉 F0 或无有效等级用户。
		Map<Integer, List<UserInfo>> userMap = users.stream()
			.filter(user -> effectiveLevel(user) > 0)
			.collect(Collectors.groupingBy(this::effectiveLevel));
		List<StakeHostingGlobalDividendDetail> details = new ArrayList<>();
		for (UserLevelConfig config : configs) {
			// 5. 每个等级先计算等级奖池，再按该等级内用户 dividend_weight 占比分配。
			List<UserInfo> levelUsers = userMap.get(config.getLevel());
			if (CollectionUtil.isEmpty(levelUsers)) {
				continue;
			}
			BigDecimal levelPool = poolAmount.multiply(config.getGlobalFeeDividendRatio())
				.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			if (levelPool.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			BigDecimal levelDividendWeight = levelUsers.stream()
				.map(user -> snapshotDividendWeight(snapshotMap, user.getUserId()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			if (levelDividendWeight.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			for (UserInfo user : levelUsers) {
				// 6. 用户分红 = 等级奖池 * 用户分红权重 / 等级总分红权重。
				BigDecimal userDividendWeight = snapshotDividendWeight(snapshotMap, user.getUserId());
				BigDecimal rewardAmount = levelPool.multiply(userDividendWeight)
					.divide(levelDividendWeight, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
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
				detail.setUserCommunityPerformance(userDividendWeight);
				detail.setLevelCommunityPerformance(levelDividendWeight);
				detail.setRewardAmount(rewardAmount);
				detail.setCreateTime(new Date());
				details.add(detail);
			}
		}
		return details;
	}

	/**
	 * 从本周快照 map 中读取用户本期分红权重。
	 *
	 * @param snapshotMap 本周用户快照 map
	 * @param userId 用户ID
	 * @return 用户分红权重；快照不存在时返回 0
	 */
	private BigDecimal snapshotDividendWeight(Map<Long, StakeHostingGlobalDividendWeightSnapshot> snapshotMap, Long userId) {
		StakeHostingGlobalDividendWeightSnapshot snapshot = snapshotMap.get(userId);
		return snapshot == null ? BigDecimal.ZERO : nvl(snapshot.getDividendWeight());
	}

	/**
	 * 将实际生成分红明细的快照标记为已参与分红。
	 *
	 * <p>未生成明细的用户仍保持 `settle_status=0`，包括权重未上涨、无有效用户资格、
	 * 无有效 F 等级或所在等级无奖池的用户。这样后台能看到完整快照，同时只有真实发放用户绑定批次号。</p>
	 *
	 * @param batchNo 全球分红批次号
	 * @param weekStartTime 分红周开始时间，格式 yyyyMMddHHmmss
	 * @param details 已保存并发放的分红明细
	 * @param now 更新时间
	 */
	private void markWeightSnapshotSettled(String batchNo, Long weekStartTime, List<StakeHostingGlobalDividendDetail> details, Date now) {
		if (CollectionUtil.isEmpty(details)) {
			return;
		}
		// 1. 从实际发放明细中收集用户ID，避免把未参与用户的快照误标记为已参与。
		List<Long> userIds = details.stream()
			.map(StakeHostingGlobalDividendDetail::getUserId)
			.collect(Collectors.toList());
		// 2. 只更新本周这些用户的快照状态和批次号。
		stakeHostingGlobalDividendWeightSnapshotService.lambdaUpdate()
			.eq(StakeHostingGlobalDividendWeightSnapshot::getWeekStartTime, weekStartTime)
			.in(StakeHostingGlobalDividendWeightSnapshot::getUserId, userIds)
			.set(StakeHostingGlobalDividendWeightSnapshot::getSettleStatus, GLOBAL_DIVIDEND_SNAPSHOT_SETTLED)
			.set(StakeHostingGlobalDividendWeightSnapshot::getBatchNo, batchNo)
			.set(StakeHostingGlobalDividendWeightSnapshot::getUpdateTime, now)
			.update();
	}

	/**
	 * 将可空 BigDecimal 转成 0，供分红权重计算使用。
	 *
	 * @param value 可空数值
	 * @return 原值；为空时返回 0
	 */
	private BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	/**
	 * 发放单个用户的全球分红并写入奖励记录和托管奖励汇总。
	 *
	 * <p>钱包币种为 USDT，流水来源沿用现有全球分红类型。该方法会产生用户钱包变动、
	 * RewardRecord 奖励记录以及用户托管全球分红累计统计。</p>
	 *
	 * @param batchNo 全球分红批次号，作为钱包流水来源单号
	 * @param detail 已计算好的单个用户分红明细
	 * @param now 发放时间
	 */
	private void grantGlobalDividend(String batchNo, StakeHostingGlobalDividendDetail detail, Date now) {
		// 1. 先给用户 USDT 钱包入账，失败时抛异常让整个 102 事务回滚。
		int rows = userWalletService.handerUserMoney(detail.getRewardAmount(), batchNo, detail.getUserId(), detail.getUserId(),
			ConstantType.user_money_log_source_type.type_37, ConstantType.user_money_coin_type.type_1);
		if (rows != 1) {
			throw new ServiceException("Business processing failed");
		}
		// 2. 写入奖励记录，便于用户端和后台按批次追踪本次全球分红。
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
		// 3. 累加用户托管奖励汇总中的全球分红金额。
		stakeHostingUserRewardSummaryService.addGlobalDividend(detail.getUserId(), detail.getRewardAmount());
	}

	/**
	 * 完成全球分红批次并回写实际发放结果。
	 *
	 * <p>批次创建时记录计划金额和处理中状态；明细计算完成后用实际发放金额、
	 * 实际参与人数和完成状态覆盖，供后台批次列表展示和后续审计使用。</p>
	 *
	 * @param batchId 全球分红批次主键ID
	 * @param actualAmount 实际发放金额，单位 USDT
	 * @param userCount 实际参与分红用户数
	 * @param now 批次完成时间
	 */
	private void finishGlobalDividendBatch(Long batchId, BigDecimal actualAmount, int userCount, Date now) {
		// 只更新批次结果字段，不改批次号、周期和计划金额。
		StakeHostingGlobalDividendBatch update = new StakeHostingGlobalDividendBatch();
		update.setId(batchId);
		update.setActualAmount(actualAmount);
		update.setUserCount(userCount);
		update.setStatus(GLOBAL_DIVIDEND_BATCH_FINISHED);
		update.setUpdateTime(now);
		stakeHostingGlobalDividendBatchService.updateById(update);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private StaticRewardResult distributeOne(StakeHostingOrder order, int rewardDay, Date now, StaticRewardCalculateContext context) {
		// Business processing note.
		BigDecimal todayRate = calculateStaticRate(order, rewardDay, context);
		// Business processing note.
		BigDecimal baseGrossReward = order.getStakeUsdtAmount().multiply(todayRate)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// Business processing note.
		StakeHostingAfiPledge effectiveAfiPledge = getEffectiveAfiPledge(order.getId(), context);
		BigDecimal afiAccelerateRate = getAfiAccelerateRate(effectiveAfiPledge);
		// Business processing note.
		BigDecimal grossReward = applyAfiAccelerate(baseGrossReward, afiAccelerateRate);
		// Business processing note.
		BigDecimal baseStaticRate = rateToPercent(todayRate);
		BigDecimal actualStaticRate = calculateActualStaticRate(todayRate, afiAccelerateRate);
		// Business processing note.
		BigDecimal serviceFeeRatio = getServiceFeeRatio(order);
		BigDecimal serviceFee = grossReward.multiply(serviceFeeRatio)
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal reward = grossReward.subtract(serviceFee)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		int nextRunDays = order.getRunDays() + 1;
		BigDecimal totalReward = order.getTotalStaticReward()
			.add(reward)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);

		// Business processing note.
		StakeHostingRewardSettlement staticSettlement = buildSettlement(order, null, REWARD_TYPE_STATIC_FEE, null, grossReward, serviceFeeRatio,
			serviceFee, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, reward, ARRIVAL_YES, null, rewardDay, now);

		// Business processing note.
		StakeHostingOrder update = new StakeHostingOrder();
		update.setId(order.getId());
		update.setTodayReward(reward);
		update.setTotalStaticReward(totalReward);
		update.setRunDays(nextRunDays);
		update.setLastRewardDay(rewardDay);
		update.setIsReturnPrincipal(totalReward.compareTo(order.getStakeUsdtAmount()) >= 0 ? 1 : 0);
		update.setUpdateTime(now);
		// Business processing note.
		boolean finished = nextRunDays >= order.getPackageDays();
		if (finished) {
			update.setStatus(StakeHostingOrderServiceImpl.STATUS_FINISHED);
			update.setFinishTime(now);
		}
		if (!stakeHostingOrderService.updateById(update)) {
			throw new ServiceException("Business processing failed");
		}
		// Business processing note.
		return new StaticRewardResult(order, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, reward, staticSettlement, finished);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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

		// Business processing note.
		refundFinishedUserPrincipal(finishedResults, now);

		Map<Long, Long> finishedUserOrderMap = new HashMap<>();
		for (StaticRewardResult result : finishedResults) {
			StakeHostingOrder order = result.order;
			// Business processing note.
			stakeHostingOrderService.subtractHostingPerformance(order.getUserId(), order.getStakeUsdtAmount(), order.getId());
			// Business processing note.
			stakeHostingAfiPledgeService.returnPledgeByOrderId(order.getId());
			// Business processing note.
			finishedUserOrderMap.putIfAbsent(order.getUserId(), order.getId());
		}
		for (Map.Entry<Long, Long> entry : finishedUserOrderMap.entrySet()) {
			stakeHostingOrderService.refreshUserValidByUnfinishedHostingOrder(entry.getKey());
			stakeHostingOrderService.sendStakeHostingLevelRecalculateAfterCommit(entry.getValue());
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private void refundFinishedUserPrincipal(List<StaticRewardResult> finishedResults, Date now) {
		if (CollectionUtil.isEmpty(finishedResults)) {
			return;
		}
		int batchSize = 1000;
		List<UserMoney> userMoneyList = new ArrayList<>(Math.min(finishedResults.size(), batchSize));
		for (StaticRewardResult result : finishedResults) {
			StakeHostingOrder order = result.order;
			// Business processing note.
			if (order.getSourceType() == null || order.getSourceType() != StakeHostingOrderServiceImpl.SOURCE_USER) {
				continue;
			}
			// Business processing note.
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
	 *
	 *
	 *
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
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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
			// Business processing note.
			UserMoney userMoney = new UserMoney();
			userMoney.setId(result.order.getUserId());
			userMoney.setValidNum1(result.netReward);
			userMoney.setGtId(gtId);
			userMoney.setSourceCode(result.order.getOrderNo());
			userMoney.setSourceId(result.order.getUserId());
			userMoney.setSourceType(ConstantType.user_money_log_source_type.type_31);
			userMoney.setUpdateTime(now);
			userMoneyList.add(userMoney);

			// Business processing note.
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
	 *
	 *
	 *
	 *
	 */
	private void flushStaticRewardBatch(List<UserMoney> userMoneyList, List<RewardRecord> rewardRecordList) {
		if (CollectionUtil.isNotEmpty(userMoneyList)) {
			// Business processing note.
			batchUpdateMoneyValid1(userMoneyList);
			userMoneyList.clear();
		}
		if (CollectionUtil.isNotEmpty(rewardRecordList)) {
			rewardRecordService.saveBatch(rewardRecordList);
			rewardRecordList.clear();
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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

		// Business processing note.
		context.pureStaticRateBeforeReturnPercent = new BigDecimal(sysParaServiceImpl.getValue(ConstantSys.PURE_STATIC_RATE_BEFORE_RETURN_PERCENT));
		context.pureStaticRateAfterReturnPercent = new BigDecimal(sysParaServiceImpl.getValue(ConstantSys.PURE_STATIC_RATE_AFTER_RETURN_PERCENT));

		// Business processing note.
		List<UserInfo> users = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, userIds)
			.list();
		if (CollectionUtil.isNotEmpty(users)) {
			context.userMap = users.stream()
				.collect(Collectors.toMap(UserInfo::getUserId, Function.identity(), (a, b) -> a));
		}

		// Business processing note.
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

		// Business processing note.
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
	 *
	 *
	 *
	 */
	private static class StaticRewardCalculateContext {
		private Map<Long, UserInfo> userMap = new HashMap<>();
		private Map<Long, StakeHostingDailyTeamPerformance> snapshotMap = new HashMap<>();
		private Map<Long, StakeHostingAfiPledge> afiPledgeMap = new HashMap<>();
		private BigDecimal pureStaticRateBeforeReturnPercent = BigDecimal.ZERO;
		private BigDecimal pureStaticRateAfterReturnPercent = BigDecimal.ZERO;
	}

	/**
	 *
	 *
	 *
	 *
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
		 *
		 *
		 *
		 */
		private boolean shouldDistributeTeamReward() {
			return order.getSourceType() != null
				&& order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_USER
				&& netReward.compareTo(BigDecimal.ZERO) > 0;
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private StakeHostingAfiPledge getEffectiveAfiPledge(Long orderId, StaticRewardCalculateContext context) {
		if (orderId == null) {
			return null;
		}
		// Business processing note.
		return context.afiPledgeMap.get(orderId);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private BigDecimal getAfiAccelerateRate(StakeHostingAfiPledge pledge) {
		if (pledge == null || pledge.getAccelerateRate() == null || pledge.getAccelerateRate().compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ONE;
		}
		return pledge.getAccelerateRate();
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 */
	private BigDecimal applyAfiAccelerate(BigDecimal baseGrossReward, BigDecimal afiAccelerateRate) {
		return baseGrossReward.multiply(afiAccelerateRate)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	/**
	 * 计算单笔托管订单当天使用的基础静态收益率乘数。
	 *
	 * <p>收益率优先级为：用户后台指定收益率、纯静态规则、G7快照收益率。未推广快照
	 * {@code rate_source=3} 只是用户维度的G7计算结果，真实发放必须回到订单维度，
	 * 按订单是否已回本读取回本前/回本后纯静态系统参数。</p>
	 *
	 * @param order 托管订单，使用订单的用户ID和是否回本状态
	 * @param rewardDay 收益日期，格式yyyyMMdd；当前方法通过context读取该日快照
	 * @param context 101任务预加载上下文，包含用户、G7快照和纯静态系统参数
	 * @return 基础静态收益率乘数，例如0.005表示0.5%
	 */
	private BigDecimal calculateStaticRate(StakeHostingOrder order, int rewardDay, StaticRewardCalculateContext context) {
		// 保留历史测试开关注释，正式发放不启用强制测试收益率。
//		if (FORCE_TEST_STATIC_RATE) {
//			return percentToRate(TEST_STATIC_RATE_PERCENT);
//		}

		// 用户后台指定收益率最高优先级，单位是%，这里转换为乘数参与订单收益计算。
		UserInfo user = context.userMap.get(order.getUserId());
		if (user != null && user.getStakeHostingStaticRate() != null && user.getStakeHostingStaticRate().compareTo(BigDecimal.ZERO) > 0) {
			return percentToRate(user.getStakeHostingStaticRate());
		}
		StakeHostingDailyTeamPerformance snapshot = context.snapshotMap.get(order.getUserId());
		boolean returnedPrincipal = order.getIsReturnPrincipal() != null && order.getIsReturnPrincipal() == 1;
		if (snapshot == null || Integer.valueOf(RATE_SOURCE_PURE_STATIC).equals(snapshot.getRateSource())) {
			// 没有G7快照，或快照明确为未推广规则时，按订单是否回本选择0.5/0.2等纯静态参数。
			BigDecimal pureStaticRate = loadPureStaticRatePercent(context, returnedPrincipal);
			return percentToRate(pureStaticRate);
		}
		if (snapshot.getBaseStaticRate() == null) {
			return PLACEHOLDER_STATIC_RATE;
		}
		// G7区间快照的收益率是用户当天团队新增窗口计算结果，可直接用于该用户订单。
		return percentToRate(snapshot.getBaseStaticRate());
	}

	/**
	 * 测算单笔托管订单当天会命中的基础静态收益率。
	 *
	 * <p>该方法不发放收益，只返回测试DTO并与真实发放的收益率选择规则保持一致；
	 * 当G7快照为未推广规则时，同样按订单是否已回本读取纯静态系统参数。</p>
	 *
	 * @param order 托管订单
	 * @param rewardDay 收益日期，格式yyyyMMdd
	 * @param context 101任务预加载上下文
	 * @return 静态收益率测试DTO，finalStaticRate单位为%
	 */
	private StakeHostingStaticRateTestDto calculateStaticRateForTest(StakeHostingOrder order, int rewardDay,
																	 StaticRewardCalculateContext context) {
		UserInfo user = context.userMap.get(order.getUserId());
		StakeHostingDailyTeamPerformance snapshot = context.snapshotMap.get(order.getUserId());
		BigDecimal finalStaticRate;
		String rateSource;
		String remark;
		boolean returnedPrincipal = order.getIsReturnPrincipal() != null && order.getIsReturnPrincipal() == 1;
		if (user != null && user.getStakeHostingStaticRate() != null
			&& user.getStakeHostingStaticRate().compareTo(BigDecimal.ZERO) > 0) {
			finalStaticRate = user.getStakeHostingStaticRate();
			rateSource = "user_config";
			remark = "Static rate source: user config";
		} else if (snapshot == null || Integer.valueOf(RATE_SOURCE_PURE_STATIC).equals(snapshot.getRateSource())) {
			finalStaticRate = loadPureStaticRatePercent(context, returnedPrincipal);
			rateSource = "pure_static";
			remark = returnedPrincipal
				? "Pure static rate after return; snapshot is missing or pure-static source"
				: "Pure static rate before return; snapshot is missing or pure-static source";
		} else if (snapshot.getBaseStaticRate() == null) {
			finalStaticRate = rateToPercent(PLACEHOLDER_STATIC_RATE);
			rateSource = "snapshot_placeholder";
			remark = "G7 snapshot base_static_rate is empty, use placeholder rate";
		} else {
			finalStaticRate = snapshot.getBaseStaticRate();
			rateSource = "g7_snapshot";
			remark = "Business processing remark";
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
	 *
	 *
	 *
	 *
	 */
	private BigDecimal rateToPercent(BigDecimal rate) {
		return rate.multiply(PERCENT_DIVISOR)
			.setScale(4, ConstantStatic.roundingModeNew);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 */
	private BigDecimal calculateActualStaticRate(BigDecimal baseRate, BigDecimal afiAccelerateRate) {
		return baseRate.multiply(afiAccelerateRate)
			.multiply(PERCENT_DIVISOR)
			.setScale(4, ConstantStatic.roundingModeNew);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private BigDecimal getServiceFeeRatio(StakeHostingOrder order) {
		if (order == null || order.getServiceFeeRatio() == null) {
			return BigDecimal.ZERO;
		}
		return order.getServiceFeeRatio();
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private void distributeTeamReward(StakeHostingOrder order, BigDecimal grossReward, BigDecimal baseStaticRate,
									  BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
									  BigDecimal serviceFeeRatio, BigDecimal serviceFee,
									  BigDecimal netReward, int rewardDay, Date now,
									  TeamRewardCollectContext context) {
		List<ParentUserTaskVo> parentUsers = getCachedParentUsers(order.getUserId(), context);
		if (CollectionUtil.isEmpty(parentUsers)) {
			log.info("Business processing log");
			return;
		}
		distributeDirectReward(order, parentUsers.get(0), grossReward, baseStaticRate, afiAccelerateRate,
			actualStaticRate, serviceFeeRatio, serviceFee, netReward, rewardDay, now, context);
		List<ParentUserTaskVo> rewardParentUsers = getCachedRewardParentUsers(order.getUserId(), context);
		if (CollectionUtil.isEmpty(rewardParentUsers)) {
			log.info("Business processing log");
			return;
		}
		distributeDiffAndSameLevelReward(order, rewardParentUsers, grossReward, baseStaticRate, afiAccelerateRate,
			actualStaticRate, serviceFeeRatio, serviceFee, netReward, rewardDay, now, context);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private void distributeDirectReward(StakeHostingOrder order, ParentUserTaskVo directUser, BigDecimal grossReward,
										BigDecimal baseStaticRate, BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
										BigDecimal serviceFeeRatio, BigDecimal serviceFee, BigDecimal netReward,
										int rewardDay, Date now, TeamRewardCollectContext context) {
		// Business processing note.
		BigDecimal directRatioPercent = getCachedDirectRewardRatioPercent(context);
		BigDecimal directReward = calculateReward(netReward, directRatioPercent);
		if (directUser == null) {
			// Business processing note.
			log.info("Business processing log");
			return;
		}
		Integer skipReason = getRewardSkipReason(directUser);
		if (skipReason != null) {
			// Business processing note.
//			collectSkippedSettlement(context, order, directUser.getUserId(), REWARD_TYPE_PLATFORM, effectiveLevel(directUser), netReward, directRatioPercent,
//				directReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
//				serviceFeeRatio, serviceFee, netReward, ARRIVAL_NO, skipReason, rewardDay, now);
			return;
		}
		// Business processing note.
		collectTeamReward(context, order, directUser.getUserId(), REWARD_TYPE_DIRECT, effectiveLevel(directUser), netReward, directRatioPercent,
			directReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, netReward, rewardDay, now);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private void distributeDiffAndSameLevelReward(StakeHostingOrder order, List<ParentUserTaskVo> parentUsers,
												  BigDecimal grossReward, BigDecimal baseStaticRate,
												  BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
												  BigDecimal serviceFeeRatio, BigDecimal serviceFee,
												  BigDecimal netReward, int rewardDay, Date now,
												  TeamRewardCollectContext context) {
		// Business processing note.
		// Same-level rule: later same-level users cause the first user to yield half of this diff pool.
		Map<Integer, BigDecimal> levelRatioMap = getCachedLevelRatioMap(context);
		// Business processing note.
		BigDecimal coveredRatio = BigDecimal.ZERO;
		for (int i = 0; i < parentUsers.size(); i++) {
			ParentUserTaskVo parent = parentUsers.get(i);
			Integer level = effectiveLevel(parent);
			BigDecimal levelRatio = levelRatioMap.getOrDefault(level, BigDecimal.ZERO);
			if (levelRatio.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			// Business processing note.
			BigDecimal diffRatio = levelRatio.subtract(coveredRatio);
			if (diffRatio.compareTo(BigDecimal.ZERO) > 0) {
				// Business processing note.
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
					// Business processing note.
					collectTeamReward(context, order, diffUser.getUserId(), REWARD_TYPE_DIFF, level, netReward, diffRatio,
						firstDiffRewardAmount, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
						serviceFeeRatio, serviceFee, netReward, rewardDay, now);
					// Business processing note.
					coveredRatio = levelRatio;
					if (level >= 5 && hasLaterSameLevel) {
						BigDecimal sameLevelPool = diffRewardAmount.subtract(firstDiffRewardAmount);
						// Business processing note.
						collectSameLevelReward(order, parentUsers, rewardSameIndexes,
							level, netReward, diffRatio, sameLevelPool, grossReward, baseStaticRate,
							afiAccelerateRate, actualStaticRate, serviceFeeRatio, serviceFee, rewardDay, now, context);
					}
				}
				// Business processing note.
				i = sameLevelGroup.nextIndex - 1;
			}
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 * @param level 鐟滅増鎸告晶鐕滅紒娑橆槺妤?
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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
			// Business processing note.
			collectTeamReward(context, order, rewardUser.getUserId(), REWARD_TYPE_SAME_LEVEL, level, netReward, diffRatio,
				sameLevelReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
				serviceFeeRatio, serviceFee, netReward, rewardDay, now);
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 * @param level 鐟滅増鎸告晶鐕滅紒娑橆槺妤?
	 *
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
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private Integer getRewardSkipReason(ParentUserTaskVo user) {
		if (!isValidStakeHostingRewardUser(user)) {
			return SKIP_NO_ACTIVE_ORDER;
		}
		return null;
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private List<ParentUserTaskVo> getCachedParentUsers(Long userId, TeamRewardCollectContext context) {
		if (userId == null) {
			return new ArrayList<>();
		}
		return context.parentUserCache.computeIfAbsent(userId, userInfoService::getParentUserTaskVo);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private boolean isValidStakeHostingRewardUser(ParentUserTaskVo user) {
		return user != null && user.getIsValid() != null && user.getIsValid() == 1;
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private Map<Integer, BigDecimal> getCachedLevelRatioMap(TeamRewardCollectContext context) {
		if (context.levelRatioMap == null) {
			context.levelRatioMap = getLevelRatioMap();
		}
		return context.levelRatioMap;
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
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
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private BigDecimal getCachedDirectRewardRatioPercent(TeamRewardCollectContext context) {
		if (context.directRewardRatioPercent == null) {
			context.directRewardRatioPercent = getDirectRewardRatioPercent();
		}
		return context.directRewardRatioPercent;
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 */
	private BigDecimal getDirectRewardRatioPercent() {
		String value = sysParaServiceImpl.getValue(ConstantSys.biz_stake_hosting_direct_reward_ratio);
		if (StrUtil.isBlank(value)) {
			throw new ServiceException("Direct reward ratio is required");
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
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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
		// Business processing note.
		UserMoney userMoney = new UserMoney();
		userMoney.setId(receiveUserId);
		userMoney.setValidNum1(rewardAmount);
		userMoney.setGtId(gtId);
		userMoney.setSourceCode(order.getOrderNo());
		userMoney.setSourceId(order.getUserId());
		userMoney.setSourceType(moneySourceType);
		userMoney.setUpdateTime(now);
		context.userMoneyList.add(userMoney);

		// Business processing note.
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
	 *
	 *
	 *
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
	 *
	 *
	 *
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
	 *
	 *
	 *
	 */
	private void flushTeamRewardContext(TeamRewardCollectContext context) {
		if (context == null || context.isEmpty()) {
			return;
		}
		if (CollectionUtil.isNotEmpty(context.userMoneyList)) {
			// Business processing note.
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
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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
			log.error("101 USDT reward batch update failed");
			throw new ServiceException("101 USDT reward batch update failed");
		}
	}

	/**
	 *
	 *
	 *
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
		 *
		 *
		 *
		 */
		private boolean isEmpty() {
			return userMoneyList.isEmpty() && rewardRecordList.isEmpty() && settlementList.isEmpty() && summaryMap.isEmpty();
		}
	}

	/**
	 *
	 *
	 *
	 *
	 */
	private static class SameLevelGroup {
		private final List<Integer> sameIndexes;
		private final int nextIndex;

		private SameLevelGroup(List<Integer> sameIndexes, int nextIndex) {
			this.sameIndexes = sameIndexes;
			this.nextIndex = nextIndex;
		}

		/**
		 *
		 *
		 *
		 *
		 *
		 */
		private List<Integer> rewardSameIndexes() {
			if (sameIndexes.size() <= 1) {
				return new ArrayList<>();
			}
			return new ArrayList<>(sameIndexes.subList(1, sameIndexes.size()));
		}

		/**
		 *
		 *
		 *
		 *
		 */
		private static SameLevelGroup single(int index) {
			List<Integer> indexes = new ArrayList<>();
			indexes.add(index);
			return new SameLevelGroup(indexes, index + 1);
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
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
	 *
	 *
	 *
	 *
	 *
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
