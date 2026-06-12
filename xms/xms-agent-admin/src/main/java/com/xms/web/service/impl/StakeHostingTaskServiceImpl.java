package com.xms.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantSys;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.Constants;
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
import org.springframework.core.env.Environment;
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
 * 托管收益定时任务服务。
 *
 * <p>当前类承载 101 每日托管静态收益、102 周全球分红等后台定时任务。
 * 101 会同时影响订单收益累计、团队动态奖励、钱包余额、奖励记录、结算明细和全球分红奖池，
 * 修改时必须同步关注钱包流水、批量写入和任务幂等。</p>
 */
@Slf4j
@Service
@AllArgsConstructor
public class StakeHostingTaskServiceImpl implements IStakeHostingTaskService {
	/**
	 * 缺省静态收益率乘数。
	 *
	 * <p>当 G7 快照存在但基础收益率为空时使用，0.005 表示 0.5%。</p>
	 */
	private static final BigDecimal PLACEHOLDER_STATIC_RATE = new BigDecimal("0.005");
	private static final boolean FORCE_TEST_STATIC_RATE = Boolean.parseBoolean("true");
	private static final BigDecimal TEST_STATIC_RATE_PERCENT = new BigDecimal("1");
	private static final BigDecimal PERCENT_DIVISOR = new BigDecimal("100");
	private static final BigDecimal TWO = new BigDecimal("2");
	private static final String SQL_VALID_NUM1 = "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num1=valid_num1+?,source_code=?,source_type=?,source_id=? WHERE id=? ";
	private static final String SQL_VALID_NUM3 = "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num3=valid_num3+?,source_code=?,source_type=?,source_id=? WHERE id=? ";

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
	private final Environment environment;

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
	 * 发放托管订单每日静态收益。
	 *
	 * <p>这是任务101的核心入口。一次执行会按当前环境决定收益归属日：prod 使用任务执行日前一天，
	 * 非 prod 使用任务执行日，便于本地/测试环境当天创建订单后立即验证收益发放。确定收益归属日后，先准备G7收益率快照，
	 * 再逐笔计算产出中托管订单的静态收益。静态收益成功后，会继续触发上级动态奖励、批量写钱包、
	 * 保存奖励记录和收益结算明细，最后处理到期订单的本金退还、业绩回退、AFI质押退还和全球分红奖池入账。</p>
	 *
	 * <p>日期口径：`executeDay` 表示定时任务实际执行日，用于 `xms_task.task_value`；`rewardDay`
	 * 表示收益归属日，用于G7快照、结算明细、订单 `last_reward_day` 和服务费奖池归属。
	 * prod 环境凌晨发昨日收益时，两者不能混用；非 prod 环境为了测试可直接发当天收益。</p>
	 *
	 * <p>幂等口径：理论上任务按 `xms_task(task_type=101, task_value=executeDay)` 做日级幂等；
	 * `last_reward_day` 记录订单最近一次收益归属日，当前不作为强制过滤条件。注意：本地测试期间
	 * `addDailyTask(executeDay)` 被注释时，同一天重复执行会依赖人工控制，不能当成生产幂等。</p>
	 *
	 * <p>钱包口径：用户购买单静态/动态收益进入 `valid_num1`；后台拨付单在订单开关开启后，
	 * 静态收益用 sourceType=47 入 `valid_num3`，动态收益用 sourceType=48 按订单收益分配方式选择入上级
	 * `valid_num3` 或 `valid_num1`。</p>
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void distributeDailyStaticReward() {
		String executeDay = DateUtil.format(DateUtil.date(), "yyyyMMdd");

		int rewardDay = resolveDailyStaticRewardDay();
		// 1. 任务级幂等：执行日已有101任务记录时，整批静态收益不再重复发放。
		Map<String, Object> task = asyncTaskServiceImpl.getTask(SysConstant.TSK_TYPE_101, executeDay);
		if (!CollectionUtil.isEmpty(task)) {
			log.debug("Task already exists");
			return;
		}

		// 2. 扫描本轮可产出的托管订单：必须已支付、产出中、未删除，并且订单创建日不能晚于收益归属日。
		//    现在业务是创建成功即支付成功，所以用 createDay<=rewardDay 控制订单从收益归属日开始参与发放。
		//    这里没有用 last_reward_day 过滤，是为了保留你本地重复测试101的便利；生产恢复任务标记时要重点确认幂等边界。
		List<StakeHostingOrder> orderList = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.eq(StakeHostingOrder::getDeleted, DELETED_NO)
			.le(StakeHostingOrder::getCreateDay, rewardDay)
			//.and(wrapper -> wrapper.ne(StakeHostingOrder::getLastRewardDay, rewardDay).or().isNull(StakeHostingOrder::getLastRewardDay))
			.list();
		if (CollectionUtil.isEmpty(orderList)) {
			log.info("101托管静态收益跳过，没有可发放的收益归属日产出中订单 executeDay={}, rewardDay={}", executeDay, rewardDay);
			// 本地测试期间保留不写任务完成标记；生产环境如果要靠 xms_task 防重，需要恢复 addDailyTask。
			//addDailyTask(executeDay);
			return;
		}
		List<Long> rewardUserIds = orderList.stream()
			.map(StakeHostingOrder::getUserId)
			.distinct()
			.collect(Collectors.toList());
		// 3. 准备G7快照：只计算收益率来源，不发钱。后续 calculateStaticRate 会按
		//    用户指定收益率 -> 纯静态规则 -> G7快照 的优先级选择基础日收益率。
		stakeHostingDailyTeamPerformanceService.prepareDailySnapshots(rewardDay, rewardUserIds);
		StaticRewardCalculateContext staticContext = buildStaticRewardCalculateContext(orderList, rewardDay);
		Date now = new Date();
		BigDecimal dailyServiceFee = BigDecimal.ZERO;
		List<StaticRewardResult> staticRewardResults = new ArrayList<>(orderList.size());
		// 4. 逐笔计算静态收益：distributeOne 会更新订单 todayReward、totalStaticReward、runDays、
		//    lastRewardDay，并在非自动复投订单到期时把订单置为已完成；返回null表示订单被跳过。
		for (StakeHostingOrder order : orderList) {
			StaticRewardResult result = distributeOne(order, rewardDay, now, staticContext);
			if (result == null) {
				continue;
			}
			// 收集成功发放的订单结果，后续钱包入账、动态奖励、退本和奖池入账都只基于这些成功结果。
			staticRewardResults.add(result);
			// 每笔静态收益扣出的服务费先在内存累加，任务末尾统一进入全球分红奖池。
			dailyServiceFee = dailyServiceFee.add(result.serviceFee);
		}
		if (CollectionUtil.isEmpty(staticRewardResults)) {
			// 所有订单都被开关、状态抢占或并发变化跳过时，不产生钱包、结算明细和奖池入账。
			// 本地测试期间保留不写任务完成标记；生产环境如果要靠 xms_task 防重，需要恢复 addDailyTask。
			//addDailyTask(executeDay);
			return;
		}
		// 5. 先保存静态收益结算明细，记录毛收益、服务费、净收益、基础收益率、AFI加速倍率等审计字段。
		saveStaticRewardSettlements(staticRewardResults);
		// 6. 再批量发放静态净收益并写奖励记录：用户购买入 valid_num1，后台拨付入 valid_num3。
		grantStaticRewards(staticRewardResults, now);
		TeamRewardCollectContext teamRewardContext = new TeamRewardCollectContext();
		// 7. 静态净收益大于0后触发团队动态奖励。动态奖励使用静态净收益作为基数，
		//    直推、极差、平级只先收集到上下文，真正钱包入账在 flushTeamRewardContext。
		for (StaticRewardResult result : staticRewardResults) {
			if (result.shouldDistributeTeamReward()) {
				// 用户购买单动态收益进入可用USDT；后台拨付单仅在订单开关开启后触发动态，并按订单收益分配方式选择可用/锁定USDT。
				distributeTeamReward(result.order, result.grossReward, result.baseStaticRate, result.afiAccelerateRate,
					result.actualStaticRate, result.serviceFeeRatio, result.serviceFee, result.netReward, rewardDay, now,
					teamRewardContext);
			}
		}
		// 8. 统一落地团队动态奖励：批量更新钱包、保存 RewardRecord、保存动态结算明细、更新普通奖励汇总。
		flushTeamRewardContext(teamRewardContext);
		// 9. 处理到期完成订单：用户购买单退还USDT本金；后台拨付单标记无需退本；
		//    同时回退托管业绩、退还AFI质押，并在事务提交后触发等级重算消息。
		handleFinishedOrdersAfterRewards(staticRewardResults, now);
		// 10. 每笔静态收益扣出的服务费进入每日全球分红奖池，后续由102周分红任务按权重分配。
		stakeHostingGlobalDividendPoolService.incomeDailyServiceFee(rewardDay,
			dailyServiceFee.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew), "task101");
		// 本地测试期间保留不写任务完成标记；生产环境如果要靠 xms_task 防重，需要恢复 addDailyTask。
		//addDailyTask(executeDay);
	}

	/**
	 * 写入101任务执行日完成标记。
	 *
	 * @param executeDay 任务执行日，格式yyyyMMdd
	 */
	private void addDailyTask(String executeDay) {
		int rows = asyncTaskServiceImpl.addTask(SysConstant.TSK_TYPE_101, executeDay);
		if (rows != 1) {
			throw new RuntimeException("Add 101 daily task failed");
		}
	}

	/**
	 * 根据运行环境确定101静态收益的收益归属日。
	 *
	 * <p>prod 环境按正式业务发昨日收益；非 prod 环境按当天作为收益归属日，方便本地或测试环境验证当天创建的托管订单。
	 * 这里只决定收益归属日，不改变 `executeDay` 任务执行日和 `xms_task` 幂等键。</p>
	 *
	 * @return 收益归属日，格式yyyyMMdd
	 */
	private int resolveDailyStaticRewardDay() {
		String profile = environment.getProperty(Constants.ACTIVE_PROFILES_PROPERTY);
		boolean prod = Constants.ACTIVE_PROPERTY_PROD.equalsIgnoreCase(profile);
		int rewardDay = Integer.parseInt(DateUtil.format(prod ? DateUtil.yesterday() : DateUtil.date(), "yyyyMMdd"));
		log.info("101托管静态收益日期口径 profile={}, prod={}, rewardDay={}", profile, prod, rewardDay);
		return rewardDay;
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

		// 5. 按本周快照的 dividend_weight 计算用户实发明细，同时统计所有启用等级奖池的本期消耗金额。
		GlobalDividendBuildResult buildResult = buildGlobalDividendDetails(batchNo, poolAmount, weekStartTime);
		List<StakeHostingGlobalDividendDetail> details = buildResult.details;
		BigDecimal actualAmount = buildResult.actualAmount;
		BigDecimal consumedAmount = buildResult.consumedAmount;
		if (consumedAmount.compareTo(BigDecimal.ZERO) <= 0) {
			log.info("102 weekly global dividend produced no consumable level pool, batchNo={}, weekStartTime={}", batchNo, weekStartTime);
			// 6. 没有任何可消耗等级奖池时仍结束批次并写入任务记录，不扣减奖池。
			finishGlobalDividendBatch(batch.getId(), actualAmount, 0, now);
			addWeeklyTask(strDate);
			return;
		}

		if (CollectionUtil.isNotEmpty(details)) {
			// 7. 保存分红明细，明细字段 userCommunityPerformance/levelCommunityPerformance 兼容保存分红权重。
			stakeHostingGlobalDividendDetailService.saveBatch(details);
			// 8. 逐条发放用户 USDT 钱包并写入 RewardRecord，保持现有全球分红发放链路不变。
			for (StakeHostingGlobalDividendDetail detail : details) {
				grantGlobalDividend(batchNo, detail, now);
			}
			// 9. 仅将实际生成分红明细的用户快照标记为已参与，并写入本批次号。
			markWeightSnapshotSettled(batchNo, weekStartTime, details, now);
		} else {
			log.info("102 weekly global dividend consumed level pools without payable users, batchNo={}, consumedAmount={}",
				batchNo, consumedAmount);
		}
		// 10. 按等级配置切出的本期消耗金额扣减奖池；该金额可能大于用户实际到账金额。
		stakeHostingGlobalDividendPoolService.expenseWeeklyDividend(batchNo, consumedAmount, "task102");
		// 11. 回写批次实际金额和人数，最后写入 102 完成记录，防止当天重复执行。
		finishGlobalDividendBatch(batch.getId(), actualAmount, details.size(), now);
		addWeeklyTask(strDate);
	}

	/**
	 * 测算托管订单在指定日期会使用的静态收益率。
	 *
	 * <p>该方法只用于后台/测试查看，不发放收益、不改订单、不写钱包。它会复用101的G7快照准备
	 * 和收益率选择逻辑，帮助产品、测试核对某天某订单最终命中的收益率来源。</p>
	 *
	 * @param rewardDay 收益日期，格式yyyyMMdd；为空时按101口径使用昨日
	 * @return 每笔产出中订单的静态收益率测算结果
	 */
	@Override
	public List<StakeHostingStaticRateTestDto> testCalculateStaticRate(Integer rewardDay) {
		/*int statDay = rewardDay == null ? Integer.parseInt(DateUtil.format(DateUtil.yesterday(), "yyyyMMdd")) : rewardDay;
		List<StakeHostingOrder> orderList = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.eq(StakeHostingOrder::getDeleted, DELETED_NO)
			.le(StakeHostingOrder::getCreateDay, statDay)
			.list();
		if (CollectionUtil.isEmpty(orderList)) {
			log.info("托管静态收益率测算跳过，当前没有产出中订单 rewardDay={}", statDay);
			return new ArrayList<>();
		}
		List<Long> rewardUserIds = orderList.stream()
			.map(StakeHostingOrder::getUserId)
			.distinct()
			.collect(Collectors.toList());
		// 测算时也先准备收益归属日G7快照，确保收益率来源与正式101发放一致。
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
		return results;*/
		//暂时不使用
		return null;
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
		// 2. 批量读取每个用户本周之前最近一期快照，作为上一期权重和上一期小区业绩基准。
		Map<Long, StakeHostingGlobalDividendWeightSnapshot> previousSnapshotMap = stakeHostingGlobalDividendWeightSnapshotService
			.selectLatestBeforeWeek(weekStartTime)
			.stream()
			.collect(Collectors.toMap(StakeHostingGlobalDividendWeightSnapshot::getUserId, snapshot -> snapshot, (a, b) -> a));
		List<StakeHostingGlobalDividendWeightSnapshot> snapshots = new ArrayList<>(users.size());
		for (UserInfo user : users) {
			// 3. 本期分红权重只取小区权重上涨部分，下降或持平都保存快照但 dividend_weight 记 0。
			BigDecimal communityWeight = nvl(user.getGlobalDividendCommunityWeight());
			StakeHostingGlobalDividendWeightSnapshot previousSnapshot = previousSnapshotMap.get(user.getUserId());
			BigDecimal previousCommunityWeight = previousSnapshot == null ? BigDecimal.ZERO : nvl(previousSnapshot.getCommunityWeight());
			BigDecimal currentCommunityPerformance = nvl(user.getCommunityPerformance());
			BigDecimal previousCommunityPerformance = previousSnapshot == null
				? BigDecimal.ZERO
				: nvl(previousSnapshot.getCurrentCommunityPerformance());
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
			snapshot.setCurrentCommunityPerformance(currentCommunityPerformance);
			snapshot.setPreviousCommunityPerformance(previousCommunityPerformance);
			snapshot.setDividendWeight(dividendWeight);
			snapshot.setDividendLevel(effectiveLevel(user));
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
	 * @return 待保存和发放的分红明细，以及本期奖池消耗金额
	 */
	private GlobalDividendBuildResult buildGlobalDividendDetails(String batchNo, BigDecimal poolAmount, Long weekStartTime) {
		// 1. 读取开启全球分红比例的 F 等级配置，等级奖池按该比例从总奖池中切分。
		List<UserLevelConfig> configs = userLevelConfigService.lambdaQuery()
			.gt(UserLevelConfig::getLevel, 0)
			.gt(UserLevelConfig::getGlobalFeeDividendRatio, BigDecimal.ZERO)
			.list();
		if (CollectionUtil.isEmpty(configs)) {
			return GlobalDividendBuildResult.empty();
		}
		// 2. 读取本周正向差值快照，dividend_weight <= 0 的用户本期不参与分红。
		List<StakeHostingGlobalDividendWeightSnapshot> snapshots = stakeHostingGlobalDividendWeightSnapshotService.lambdaQuery()
			.eq(StakeHostingGlobalDividendWeightSnapshot::getWeekStartTime, weekStartTime)
			.eq(StakeHostingGlobalDividendWeightSnapshot::getDeleted, DELETED_NO)
			.gt(StakeHostingGlobalDividendWeightSnapshot::getDividendWeight, BigDecimal.ZERO)
			.list();
		Map<Long, StakeHostingGlobalDividendWeightSnapshot> snapshotMap = CollectionUtil.isEmpty(snapshots)
			? new HashMap<>()
			: snapshots.stream()
				.collect(Collectors.toMap(StakeHostingGlobalDividendWeightSnapshot::getUserId, snapshot -> snapshot, (a, b) -> a));
		// 3. 批量读取正向差值用户中的当前有效用户，未持有有效托管订单的用户保留快照但不分红。
		Map<Integer, List<UserInfo>> userMap = new HashMap<>();
		if (!snapshotMap.isEmpty()) {
			List<UserInfo> users = userInfoService.lambdaQuery()
				.eq(UserInfo::getIsValid, 1)
				.eq(UserInfo::getDeleted, DELETED_NO)
				.in(UserInfo::getUserId, new ArrayList<>(snapshotMap.keySet()))
				.list();
			if (CollectionUtil.isNotEmpty(users)) {
				// 4. 按有效 F 等级分组；effectiveLevel 会过滤掉 F0 或无有效等级用户。
				userMap = users.stream()
					.filter(user -> effectiveLevel(user) > 0)
					.collect(Collectors.groupingBy(this::effectiveLevel));
			}
		}
		List<StakeHostingGlobalDividendDetail> details = new ArrayList<>();
		BigDecimal consumedAmount = BigDecimal.ZERO;
		for (UserLevelConfig config : configs) {
			// 5. 每个开启比例的等级奖池都会消耗奖池；有可分用户时再按用户 dividend_weight 占比分配到账。
			BigDecimal levelPool = poolAmount.multiply(config.getGlobalFeeDividendRatio())
				.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			if (levelPool.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			consumedAmount = consumedAmount.add(levelPool);
			List<UserInfo> levelUsers = userMap.get(config.getLevel());
			if (CollectionUtil.isEmpty(levelUsers)) {
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
		BigDecimal actualAmount = details.stream()
			.map(StakeHostingGlobalDividendDetail::getRewardAmount)
			.reduce(BigDecimal.ZERO, BigDecimal::add)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		return new GlobalDividendBuildResult(details,
			consumedAmount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew), actualAmount);
	}

	private static class GlobalDividendBuildResult {
		private final List<StakeHostingGlobalDividendDetail> details;
		private final BigDecimal consumedAmount;
		private final BigDecimal actualAmount;

		private GlobalDividendBuildResult(List<StakeHostingGlobalDividendDetail> details, BigDecimal consumedAmount,
										  BigDecimal actualAmount) {
			this.details = details;
			this.consumedAmount = consumedAmount;
			this.actualAmount = actualAmount;
		}

		private static GlobalDividendBuildResult empty() {
			return new GlobalDividendBuildResult(new ArrayList<>(), BigDecimal.ZERO, BigDecimal.ZERO);
		}
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
	 * 计算并抢占发放单笔托管订单的当日静态收益。
	 *
	 * <p>用户购买的 1 天托管订单命中自动复投规则时，只更新收益累计和最近发放日，不把订单置为完成。
	 * 其他订单仍按套餐天数到期完成。返回 {@code null} 表示订单已被其他并发任务处理，本轮静默跳过。</p>
	 *
	 * <p>核心公式：
	 * 基础毛收益 = 托管USDT金额 * 基础静态日收益率乘数；
	 * 加速毛收益 = 基础毛收益 * AFI加速倍率；
	 * 服务费 = 加速毛收益 * 服务费比例 / 100；
	 * 静态净收益 = 加速毛收益 - 服务费。</p>
	 *
	 * @param order 待发放的产出中托管订单
	 * @param rewardDay 收益日期，格式 yyyyMMdd
	 * @param now 本轮任务时间
	 * @param context 静态收益计算上下文
	 * @return 本轮发放结果；为空表示订单级幂等抢占失败
	 */
	private StaticRewardResult distributeOne(StakeHostingOrder order, int rewardDay, Date now, StaticRewardCalculateContext context) {
		if (isGrantOrderRewardDisabled(order)) {
			log.info("后台拨付托管收益开关关闭，跳过静态和动态收益 orderId={}, userId={}, rewardDay={}",
				order.getId(), order.getUserId(), rewardDay);
			return null;
		}
		// 1. 读取订单收益归属日基础静态收益率乘数。优先级在 calculateStaticRate 内部处理：
		//    用户后台指定收益率 > 未推广纯静态规则 > G7快照收益率。
		BigDecimal todayRate = calculateStaticRate(order, rewardDay, context);
		// 2. 基础毛收益 = 托管USDT金额 * 基础静态日收益率乘数。
		//    例：1000U * 0.005 = 5U；这里还没有扣服务费，也还没有应用AFI加速。
		BigDecimal baseGrossReward = order.getStakeUsdtAmount().multiply(todayRate)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 3. 读取订单收益归属日已生效的AFI质押加速倍率；没有质押时倍率按1处理。
		StakeHostingAfiPledge effectiveAfiPledge = getEffectiveAfiPledge(order.getId(), context);
		BigDecimal afiAccelerateRate = getAfiAccelerateRate(effectiveAfiPledge);
		// 4. 加速毛收益 = 基础毛收益 * AFI加速倍率。
		//    例：基础毛收益5U，倍率1.2，则加速毛收益为6U。
		BigDecimal grossReward = applyAfiAccelerate(baseGrossReward, afiAccelerateRate);
		// 5. 结算明细需要保存展示口径的百分比：基础收益率和AFI加速后的实际收益率。
		BigDecimal baseStaticRate = rateToPercent(todayRate);
		BigDecimal actualStaticRate = calculateActualStaticRate(todayRate, afiAccelerateRate);
		// 6. 服务费 = 加速毛收益 * 订单服务费比例 / 100。服务费不发给用户，任务末尾统一进入全球分红奖池。
		BigDecimal serviceFeeRatio = getServiceFeeRatio(order);
		BigDecimal serviceFee = grossReward.multiply(serviceFeeRatio)
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 7. 静态净收益 = 加速毛收益 - 服务费。后续静态钱包入账、动态奖励基数都使用这个净收益。
		BigDecimal reward = grossReward.subtract(serviceFee)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 8. runDays 表示已发放静态收益次数；本轮发放成功后加1。
		int currentRunDays = order.getRunDays() == null ? 0 : order.getRunDays();
		int nextRunDays = currentRunDays + 1;
		// 9. totalStaticReward 累计的是已发静态净收益，不包含被扣出的服务费。
		BigDecimal totalReward = nvl(order.getTotalStaticReward())
			.add(reward)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);

		// 后台拨付单复用 today_reward/total_static_reward 做订单维度累计；
		// 真正资产入账会在后续批量钱包阶段处理，并使用 47/48 与真实托管收益隔离。
		// 结算明细记录静态收益、服务费、收益率和 AFI 加速倍率快照，用于后台追溯。
		StakeHostingRewardSettlement staticSettlement = buildSettlement(order, null, REWARD_TYPE_STATIC_FEE, null, grossReward, serviceFeeRatio,
			serviceFee, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, reward, ARRIVAL_YES, null, rewardDay, now);

		// 10. 1天用户购买单命中自动复投：即使 nextRunDays >= packageDays，也不改成已完成，
		//     继续保持产出中，直到用户后续在App点击停止托管才退本金。
		boolean autoReinvest = isUserPurchasedOneDayOrder(order);
		boolean finished = !autoReinvest && nextRunDays >= order.getPackageDays();
		// 11. 带状态条件更新订单，避免已被其他流程改成非产出中的订单继续写收益。
		var updateChain = stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, order.getId())
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.eq(StakeHostingOrder::getDeleted, DELETED_NO)
			.set(StakeHostingOrder::getTodayReward, reward)
			.set(StakeHostingOrder::getTotalStaticReward, totalReward)
			.set(StakeHostingOrder::getRunDays, nextRunDays)
			.set(StakeHostingOrder::getLastRewardDay, rewardDay)
			.set(StakeHostingOrder::getIsReturnPrincipal, totalReward.compareTo(order.getStakeUsdtAmount()) >= 0 ? 1 : 0)
			.set(StakeHostingOrder::getUpdateTime, now);
		if (finished) {
			updateChain
				.set(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_FINISHED)
				.set(StakeHostingOrder::getFinishTime, now);
		}
		// 12. 更新失败表示订单状态已变化或不再符合发放条件，本轮不写钱包、不写动态奖励、不写退本。
		if (!updateChain.update()) {
			log.info("托管静态收益跳过，订单收益归属日已发放或状态已变化 orderId={}, rewardDay={}", order.getId(), rewardDay);
			return null;
		}
		// 13. 返回结果对象给外层：外层会基于该对象保存静态结算明细、发钱包、发动态奖励和处理到期副作用。
		return new StaticRewardResult(order, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, reward, staticSettlement, finished);
	}

	/**
	 * 判断订单是否命中 1 天用户购买单自动复投规则。
	 *
	 * @param order 托管订单
	 * @return true 表示该订单到期后继续产出中，不自动完成和退本
	 */
	private boolean isUserPurchasedOneDayOrder(StakeHostingOrder order) {
		return order.getSourceType() != null
			&& order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_USER
			&& order.getPackageDays() != null
			&& order.getPackageDays() == 1;
	}

	/**
	 * 判断后台拨付托管订单是否因为订单维度收益开关关闭而跳过收益。
	 *
	 * @param order 待结算托管订单
	 * @return true 表示后台拨付单且订单未开启收益开关，本轮不发静态也不触发动态
	 */
	private boolean isGrantOrderRewardDisabled(StakeHostingOrder order) {
		if (!isAdminGrantOrder(order)) {
			return false;
		}
		return order.getGrantRewardEnabled() == null || order.getGrantRewardEnabled() != 1;
	}

	/**
	 * 判断订单是否为后台拨付托管订单。
	 *
	 * @param order 托管订单
	 * @return true 表示 `source_type=1` 后台拨付订单
	 */
	private boolean isAdminGrantOrder(StakeHostingOrder order) {
		return order.getSourceType() != null
			&& order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_ADMIN;
	}

	/**
	 * 处理本轮静态收益后到期完成的托管订单。
	 *
	 * <p>只有非自动复投订单会进入这里。用户购买单先退还 USDT 本金并标记退本状态；
	 * 后台拨付单没有用户实付本金，标记为无需退还。随后统一回退业绩、退还 AFI 质押并触发等级重算。</p>
	 *
	 * @param results 本轮静态收益发放结果
	 * @param now 本轮任务时间
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

		// 到期完成订单先维护本金退还状态，避免后续补偿或停止逻辑重复退本。
		refundFinishedUserPrincipal(finishedResults, now);
		markFinishedGrantPrincipalNotRequired(finishedResults, now);

		Map<Long, Long> finishedUserOrderMap = new HashMap<>();
		for (StaticRewardResult result : finishedResults) {
			StakeHostingOrder order = result.order;
			// 订单完成后回退本人、直推、伞下和全球分红权重等托管业绩影响。
			stakeHostingOrderService.subtractHostingPerformance(order.getUserId(), order.getStakeUsdtAmount(), order.getId());
			// 非 1 天套餐可能绑定 AFI 加速，到期时退回仍在质押中的 AFI。
			stakeHostingAfiPledgeService.returnPledgeByOrderId(order.getId());
			finishedUserOrderMap.putIfAbsent(order.getUserId(), order.getId());
		}
		for (Map.Entry<Long, Long> entry : finishedUserOrderMap.entrySet()) {
			stakeHostingOrderService.refreshUserValidByUnfinishedHostingOrder(entry.getKey());
			stakeHostingOrderService.sendStakeHostingLevelRecalculateAfterCommit(entry.getValue());
		}
	}

	/**
	 * 批量退还到期用户购买单的 USDT 本金。
	 *
	 * <p>只处理 `source_type=0` 且 `principal_return_status=0` 的订单。钱包入账使用
	 * `valid_num1` 和 `source_type=39`，并在同一事务内把订单本金退还状态改为已退还。</p>
	 *
	 * @param finishedResults 本轮已完成的托管订单结果
	 * @param now 本轮任务时间
	 */
	private void refundFinishedUserPrincipal(List<StaticRewardResult> finishedResults, Date now) {
		if (CollectionUtil.isEmpty(finishedResults)) {
			return;
		}
		int batchSize = 1000;
		List<UserMoney> userMoneyList = new ArrayList<>(Math.min(finishedResults.size(), batchSize));
		List<Long> returnedOrderIds = new ArrayList<>(Math.min(finishedResults.size(), batchSize));
		for (StaticRewardResult result : finishedResults) {
			StakeHostingOrder order = result.order;
			if (order.getSourceType() == null || order.getSourceType() != StakeHostingOrderServiceImpl.SOURCE_USER) {
				continue;
			}
			if (order.getPrincipalReturnStatus() != null
				&& order.getPrincipalReturnStatus() != StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_WAIT) {
				continue;
			}
			if (order.getStakeUsdtAmount() == null || order.getStakeUsdtAmount().compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			// 到期本金退还入账到用户 USDT 可用余额，流水按托管订单号和订单ID追踪。
			UserMoney userMoney = new UserMoney();
			userMoney.setId(order.getUserId());
			userMoney.setValidNum1(order.getStakeUsdtAmount().setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
			userMoney.setGtId(IDUtils.getSnowflakeStr());
			userMoney.setSourceCode(order.getOrderNo());
			userMoney.setSourceId(order.getId());
			userMoney.setSourceType(ConstantType.user_money_log_source_type.type_39);
			userMoney.setUpdateTime(now);
			userMoneyList.add(userMoney);
			returnedOrderIds.add(order.getId());
			if (userMoneyList.size() >= batchSize) {
				batchUpdateMoneyValid1(userMoneyList);
				markPrincipalReturned(returnedOrderIds, now);
				userMoneyList.clear();
				returnedOrderIds.clear();
			}
		}
		batchUpdateMoneyValid1(userMoneyList);
		markPrincipalReturned(returnedOrderIds, now);
		userMoneyList.clear();
		returnedOrderIds.clear();
	}

	/**
	 * 将已完成的后台拨付单标记为无需退还本金。
	 *
	 * <p>后台拨付单没有用户链上支付本金，订单完成后不写钱包流水，只维护退本状态为 2，避免后台和后续停止逻辑误判为待退本。</p>
	 *
	 * @param finishedResults 本轮已完成的托管订单结果
	 * @param now 本轮任务时间
	 */
	private void markFinishedGrantPrincipalNotRequired(List<StaticRewardResult> finishedResults, Date now) {
		List<Long> grantOrderIds = finishedResults.stream()
			.map(result -> result.order)
			.filter(order -> order.getSourceType() != null && order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_ADMIN)
			.filter(order -> order.getPrincipalReturnStatus() == null
				|| order.getPrincipalReturnStatus() == StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_WAIT)
			.map(StakeHostingOrder::getId)
			.collect(Collectors.toList());
		if (CollectionUtil.isEmpty(grantOrderIds)) {
			return;
		}
		boolean updated = stakeHostingOrderService.lambdaUpdate()
			.in(StakeHostingOrder::getId, grantOrderIds)
			.eq(StakeHostingOrder::getPrincipalReturnStatus, StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_WAIT)
			.set(StakeHostingOrder::getPrincipalReturnStatus, StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_NOT_REQUIRED)
			.set(StakeHostingOrder::getUpdateTime, now)
			.update();
		if (!updated) {
			throw new ServiceException("Mark grant principal return status failed");
		}
	}

	/**
	 * 标记已完成退本的钱包入账订单。
	 *
	 * @param orderIds 已完成 USDT 本金退还的钱包订单ID集合
	 * @param now 本轮任务时间
	 */
	private void markPrincipalReturned(List<Long> orderIds, Date now) {
		if (CollectionUtil.isEmpty(orderIds)) {
			return;
		}
		boolean updated = stakeHostingOrderService.lambdaUpdate()
			.in(StakeHostingOrder::getId, orderIds)
			.eq(StakeHostingOrder::getPrincipalReturnStatus, StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_WAIT)
			.set(StakeHostingOrder::getPrincipalReturnStatus, StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_DONE)
			.set(StakeHostingOrder::getPrincipalReturnTime, now)
			.set(StakeHostingOrder::getUpdateTime, now)
			.update();
		if (!updated) {
			throw new ServiceException("Mark principal returned failed");
		}
	}

	/**
	 * 批量保存本轮静态收益结算明细。
	 *
	 * <p>这里只落库 `t_stake_hosting_reward_settlement`，不修改钱包。钱包发放由后续
	 * `grantStaticRewards` 分批处理，避免结算明细和资产入账逻辑混在一起。</p>
	 *
	 * @param results 本轮成功计算并更新订单的静态收益结果
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
	 * 批量发放托管静态净收益并记录奖励流水。
	 *
	 * <p>用户购买订单继续进入 `valid_num1`，钱包来源类型为31；后台拨付订单在订单开关开启后
	 * 进入 `valid_num3` 锁定USDT，钱包来源类型为47。订单本身仍复用 todayReward/totalStaticReward
	 * 做订单维度累计，资产字段和 sourceType 负责区分真实收益与拨付收益。</p>
	 *
	 * @param results 本轮静态收益计算结果
	 * @param now 本轮任务时间
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
			boolean grantReward = isAdminGrantOrder(result.order);
			String gtId = IDUtils.getSnowflakeStr();
			// 用户购买单进入可用USDT；后台拨付单进入锁定USDT，避免和真实托管收益资产混淆。
			UserMoney userMoney = new UserMoney();
			userMoney.setId(result.order.getUserId());
			if (grantReward) {
				userMoney.setValidNum3(result.netReward);
			} else {
				userMoney.setValidNum1(result.netReward);
			}
			userMoney.setGtId(gtId);
			userMoney.setSourceCode(result.order.getOrderNo());
			userMoney.setSourceId(grantReward ? result.order.getId() : result.order.getUserId());
			userMoney.setSourceType(grantReward
				? ConstantType.user_money_log_source_type.type_47
				: ConstantType.user_money_log_source_type.type_31);
			userMoney.setUpdateTime(now);
			userMoneyList.add(userMoney);

			// 奖励记录同样用独立币种和来源类型隔离锁定USDT静态收益。
			RewardRecord rewardRecord = new RewardRecord();
			rewardRecord.setOrderCode(IDUtils.getSnowflakeStr());
			rewardRecord.setUserId(result.order.getUserId());
			rewardRecord.setAmount(result.netReward);
			rewardRecord.setCoinType(grantReward
				? ConstantType.user_money_coin_type.type_3
				: ConstantType.user_money_coin_type.type_1);
			rewardRecord.setSourceType(grantReward
				? ConstantType.xms_reward_record_source_type.type_47
				: ConstantType.xms_reward_record_source_type.type_27);
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
	 * flush 静态收益钱包增量和奖励记录。
	 *
	 * @param userMoneyList 待入账钱包增量，可能同时包含 valid_num1 和 valid_num3
	 * @param rewardRecordList 待保存奖励记录
	 */
	private void flushStaticRewardBatch(List<UserMoney> userMoneyList, List<RewardRecord> rewardRecordList) {
		if (CollectionUtil.isNotEmpty(userMoneyList)) {
			// 静态收益批量入账按资产字段分流：真实托管收益进 valid_num1，后台拨付收益进 valid_num3。
			batchUpdateRewardMoney(userMoneyList);
			userMoneyList.clear();
		}
		if (CollectionUtil.isNotEmpty(rewardRecordList)) {
			rewardRecordService.saveBatch(rewardRecordList);
			rewardRecordList.clear();
		}
	}

	/**
	 * 构建101静态收益计算上下文。
	 *
	 * <p>上下文会一次性预加载订单用户、当日G7快照、有效AFI质押和纯静态系统参数。
	 * 这些数据在订单循环中反复使用，提前加载可以减少N+1查询，也保证同一轮任务使用同一批快照。</p>
	 *
	 * @param orderList 本轮待发放托管订单
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return 静态收益计算上下文
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

		// 1. 纯静态收益率来自系统参数，分别对应回本前和回本后的日收益率，单位是%。
		context.pureStaticRateBeforeReturnPercent = new BigDecimal(sysParaServiceImpl.getValue(ConstantSys.PURE_STATIC_RATE_BEFORE_RETURN_PERCENT));
		context.pureStaticRateAfterReturnPercent = new BigDecimal(sysParaServiceImpl.getValue(ConstantSys.PURE_STATIC_RATE_AFTER_RETURN_PERCENT));

		// 2. 预加载订单用户信息；收益率优先级依赖用户字段，后台拨付收益开关依赖订单字段。
		List<UserInfo> users = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, userIds)
			.list();
		if (CollectionUtil.isNotEmpty(users)) {
			context.userMap = users.stream()
				.collect(Collectors.toMap(UserInfo::getUserId, Function.identity(), (a, b) -> a));
		}

		// 3. 预加载收益归属日已计算完成的G7快照，用于读取订单用户基础静态收益率。
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

		// 4. 预加载已生效的AFI质押加速记录，静态收益毛收益会按加速倍率放大。
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
	 * 101静态收益计算上下文。
	 *
	 * <p>该对象只在单轮任务内使用，用Map缓存用户、快照和AFI质押，避免循环内重复查库。</p>
	 */
	private static class StaticRewardCalculateContext {
		private Map<Long, UserInfo> userMap = new HashMap<>();
		private Map<Long, StakeHostingDailyTeamPerformance> snapshotMap = new HashMap<>();
		private Map<Long, StakeHostingAfiPledge> afiPledgeMap = new HashMap<>();
		private BigDecimal pureStaticRateBeforeReturnPercent = BigDecimal.ZERO;
		private BigDecimal pureStaticRateAfterReturnPercent = BigDecimal.ZERO;
	}

	/**
	 * 单笔托管订单本轮静态收益计算结果。
	 *
	 * <p>结果对象会继续用于三类后续动作：保存静态结算明细、批量钱包入账和触发团队动态收益。
	 * finished表示本轮静态收益后订单是否已到套餐天数并被置为完成。</p>
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
		 * 判断本次静态收益结果是否需要继续触发团队动态奖励。
		 *
		 * <p>用户购买单始终按原规则触发；后台拨付单只有在前置订单开关校验通过并产生静态收益结果后才会走到这里。</p>
		 *
		 * @return true 表示净静态收益大于0且订单来源允许触发动态奖励
		 */
		private boolean shouldDistributeTeamReward() {
			return order.getSourceType() != null
				&& (order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_USER
					|| order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_ADMIN)
				&& netReward.compareTo(BigDecimal.ZERO) > 0;
		}
	}

	/**
	 * 获取订单收益归属日已生效的 AFI 加速质押记录。
	 *
	 * <p>101 任务在进入订单循环前已经按订单ID预加载有效质押记录，这里只从上下文Map读取，
	 * 避免每笔订单单独查库。返回 null 表示该订单收益归属日没有可用加速。</p>
	 *
	 * @param orderId 托管订单ID
	 * @param context 101静态收益计算上下文
	 * @return 有效 AFI 质押记录；没有则返回 null
	 */
	private StakeHostingAfiPledge getEffectiveAfiPledge(Long orderId, StaticRewardCalculateContext context) {
		if (orderId == null) {
			return null;
		}
		// AFI质押有效性已在预加载SQL中通过 effective_day/status 过滤，这里不重复判断。
		return context.afiPledgeMap.get(orderId);
	}

	/**
	 * 读取 AFI 加速倍率。
	 *
	 * <p>accelerateRate 是倍率，不是百分比。没有有效质押或倍率小于等于0时按1倍处理，
	 * 即不放大静态毛收益。</p>
	 *
	 * @param pledge 已生效的 AFI 质押记录
	 * @return 加速倍率，例如1.2表示静态毛收益放大到1.2倍
	 */
	private BigDecimal getAfiAccelerateRate(StakeHostingAfiPledge pledge) {
		if (pledge == null || pledge.getAccelerateRate() == null || pledge.getAccelerateRate().compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ONE;
		}
		return pledge.getAccelerateRate();
	}

	/**
	 * 应用 AFI 加速倍率得到静态毛收益。
	 *
	 * <p>计算公式：基础毛收益 = 托管USDT金额 * 基础静态收益率乘数；
	 * 加速后毛收益 = 基础毛收益 * AFI加速倍率。这里统一按项目金额精度做四舍五入。</p>
	 *
	 * @param baseGrossReward 未加速的静态毛收益
	 * @param afiAccelerateRate AFI加速倍率
	 * @return 加速后的静态毛收益
	 */
	private BigDecimal applyAfiAccelerate(BigDecimal baseGrossReward, BigDecimal afiAccelerateRate) {
		return baseGrossReward.multiply(afiAccelerateRate)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	/**
	 * 计算单笔托管订单收益归属日使用的基础静态收益率乘数。
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
		// G7区间快照的收益率是用户收益归属日团队新增窗口计算结果，可直接用于该用户订单。
		return percentToRate(snapshot.getBaseStaticRate());
	}

	/**
	 * 测算单笔托管订单收益归属日会命中的基础静态收益率。
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
			remark = "G7 snapshot rate is used";
		}
		return StakeHostingStaticRateTestDto.builder()
			.orderId(order.getId())
			.orderNo(order.getOrderNo())
			.userId(order.getUserId())
			.stakeUsdtAmount(order.getStakeUsdtAmount())
			.stakeHostingStaticRate(user == null ? null : user.getStakeHostingStaticRate())
			.previousTeamTvl(snapshot == null ? null : snapshot.getPreviousTeamTvl())
			.currentTeamTvl(snapshot == null ? null : snapshot.getCurrentTeamTvl())
			.previousTeamTotalPerformance(snapshot == null ? null : snapshot.getPreviousTeamTotalPerformance())
			.currentTeamTotalPerformance(snapshot == null ? null : snapshot.getCurrentTeamTotalPerformance())
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
	 * 读取纯静态收益率参数。
	 *
	 * <p>没有G7快照或快照来源为未推广规则时，101 会根据订单是否已回本选择回本前/回本后纯静态参数。
	 * 参数单位是%，例如0.5表示0.5%。</p>
	 *
	 * @param context 101静态收益计算上下文
	 * @param returnedPrincipal true表示订单已回本，使用回本后收益率
	 * @return 纯静态日收益率，单位%
	 */
	private BigDecimal loadPureStaticRatePercent(StaticRewardCalculateContext context, boolean returnedPrincipal) {
		return returnedPrincipal
			? context.pureStaticRateAfterReturnPercent
			: context.pureStaticRateBeforeReturnPercent;
	}

	/**
	 * 将百分比收益率转换为收益计算乘数。
	 *
	 * <p>配置和快照中的收益率单位是%，订单收益计算使用乘数，所以需要除以100。
	 * 例如0.5%会转成0.005。</p>
	 *
	 * @param percentRate 百分比收益率，单位%
	 * @return 收益计算乘数
	 */
	private BigDecimal percentToRate(BigDecimal percentRate) {
		return percentRate.divide(PERCENT_DIVISOR, 8, ConstantStatic.roundingModeNew);
	}

	/**
	 * 将收益计算乘数转换为百分比展示值。
	 *
	 * @param rate 收益计算乘数，例如0.005
	 * @return 百分比收益率，例如0.5000
	 */
	private BigDecimal rateToPercent(BigDecimal rate) {
		return rate.multiply(PERCENT_DIVISOR)
			.setScale(4, ConstantStatic.roundingModeNew);
	}

	/**
	 * 计算最终实际静态收益率展示值。
	 *
	 * <p>计算公式：实际静态收益率(%) = 基础收益率乘数 * AFI加速倍率 * 100。
	 * 该值写入结算明细，方便后台解释“基础收益率 + AFI加速”后的真实发放比例。</p>
	 *
	 * @param baseRate 基础收益率乘数
	 * @param afiAccelerateRate AFI加速倍率
	 * @return 实际静态收益率，单位%
	 */
	private BigDecimal calculateActualStaticRate(BigDecimal baseRate, BigDecimal afiAccelerateRate) {
		return baseRate.multiply(afiAccelerateRate)
			.multiply(PERCENT_DIVISOR)
			.setScale(4, ConstantStatic.roundingModeNew);
	}

	/**
	 * 获取订单服务费比例快照。
	 *
	 * <p>服务费比例来自下单时套餐快照，单位是%。101 会用该比例从静态毛收益中扣出服务费，
	 * 服务费按收益归属日汇总后进入全球分红奖池。</p>
	 *
	 * @param order 托管订单
	 * @return 服务费比例，单位%；为空时按0处理
	 */
	private BigDecimal getServiceFeeRatio(StakeHostingOrder order) {
		if (order == null || order.getServiceFeeRatio() == null) {
			return BigDecimal.ZERO;
		}
		return order.getServiceFeeRatio();
	}

	/**
	 * 基于单笔订单静态净收益触发团队动态奖励。
	 *
	 * <p>动态奖励分两段：先给直属上级发直推奖，再按有效上级链计算极差/平级奖。
	 * 用户购买单动态奖励进入可用USDT；后台拨付单只有开关开启并产生静态收益后才会进入这里，
	 * 并按订单收益分配方式选择进入可用或锁定USDT。</p>
	 *
	 * @param order 产生静态收益的托管订单
	 * @param grossReward 静态毛收益
	 * @param baseStaticRate 基础静态收益率乘数
	 * @param afiAccelerateRate AFI加速倍率
	 * @param actualStaticRate 实际静态收益率，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额
	 * @param netReward 扣服务费后的静态净收益，作为动态奖励基数
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @param now 本轮任务时间
	 * @param context 团队奖励收集上下文
	 */
	private void distributeTeamReward(StakeHostingOrder order, BigDecimal grossReward, BigDecimal baseStaticRate,
									  BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
									  BigDecimal serviceFeeRatio, BigDecimal serviceFee,
									  BigDecimal netReward, int rewardDay, Date now,
									  TeamRewardCollectContext context) {
		// 1. 直属上级用于直推奖；没有上级时本订单不产生动态奖励。
		List<ParentUserTaskVo> parentUsers = getCachedParentUsers(order.getUserId(), context);
		if (CollectionUtil.isEmpty(parentUsers)) {
			log.info("托管动态奖励跳过，用户无上级 userId={}, orderId={}", order.getUserId(), order.getId());
			return;
		}
		distributeDirectReward(order, parentUsers.get(0), grossReward, baseStaticRate, afiAccelerateRate,
			actualStaticRate, serviceFeeRatio, serviceFee, netReward, rewardDay, now, context);
		// 2. 极差/平级奖只在有效上级链中计算，无有效上级时直推之后结束。
		List<ParentUserTaskVo> rewardParentUsers = getCachedRewardParentUsers(order.getUserId(), context);
		if (CollectionUtil.isEmpty(rewardParentUsers)) {
			log.info("托管极差/平级奖励跳过，用户无有效上级 userId={}, orderId={}", order.getUserId(), order.getId());
			return;
		}
		distributeDiffAndSameLevelReward(order, rewardParentUsers, grossReward, baseStaticRate, afiAccelerateRate,
			actualStaticRate, serviceFeeRatio, serviceFee, netReward, rewardDay, now, context);
	}

	/**
	 * 计算并收集直属上级直推奖励。
	 *
	 * <p>直推奖励基数为订单静态净收益，比例读取系统参数。直属上级无有效托管资格时，
	 * 当前实现直接跳过，不发放也不写未到账结算明细。</p>
	 *
	 * @param order 产生静态收益的托管订单
	 * @param directUser 直属上级
	 * @param grossReward 静态毛收益
	 * @param baseStaticRate 基础静态收益率乘数
	 * @param afiAccelerateRate AFI加速倍率
	 * @param actualStaticRate 实际静态收益率，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额
	 * @param netReward 静态净收益，作为直推奖基数
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @param now 本轮任务时间
	 * @param context 团队奖励收集上下文
	 */
	private void distributeDirectReward(StakeHostingOrder order, ParentUserTaskVo directUser, BigDecimal grossReward,
										BigDecimal baseStaticRate, BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
										BigDecimal serviceFeeRatio, BigDecimal serviceFee, BigDecimal netReward,
										int rewardDay, Date now, TeamRewardCollectContext context) {
		// 1. 直推比例是系统参数，单位是%；直推金额 = 静态净收益 * 直推比例 / 100。
		BigDecimal directRatioPercent = getCachedDirectRewardRatioPercent(context);
		BigDecimal directReward = calculateReward(netReward, directRatioPercent);
		if (directUser == null) {
			log.info("托管直推奖励跳过，直属上级为空 orderId={}", order.getId());
			return;
		}
		Integer skipReason = getRewardSkipReason(directUser);
		if (skipReason != null) {
			// 2. 直属上级没有有效托管资格时不发放；历史未到账明细逻辑保留注释，当前不落库。
//			collectSkippedSettlement(context, order, directUser.getUserId(), REWARD_TYPE_PLATFORM, effectiveLevel(directUser), netReward, directRatioPercent,
//				directReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
//				serviceFeeRatio, serviceFee, netReward, ARRIVAL_NO, skipReason, rewardDay, now);
			return;
		}
		// 3. 只收集钱包、奖励记录和结算明细，真正批量入账在 flushTeamRewardContext 统一执行。
		collectTeamReward(context, order, directUser.getUserId(), REWARD_TYPE_DIRECT, effectiveLevel(directUser), netReward, directRatioPercent,
			directReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, netReward, rewardDay, now);
	}

	/**
	 * 计算并收集有效上级链的极差奖和平级奖。
	 *
	 * <p>极差奖使用“覆盖比例”模型：上级等级比例大于已覆盖比例时，只发放差额比例。
	 * F5及以上同级用户会触发平级规则：同级组内第一人先拿极差池的一半，后续同级用户
	 * 从剩余平级池按 1/2、1/4、... 分配；低级别用户不参与分配，高级别用户会终止当前同级组。</p>
	 *
	 * @param order 产生静态收益的托管订单
	 * @param parentUsers 已过滤为有效托管资格的上级链
	 * @param grossReward 静态毛收益
	 * @param baseStaticRate 基础静态收益率乘数
	 * @param afiAccelerateRate AFI加速倍率
	 * @param actualStaticRate 实际静态收益率，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额
	 * @param netReward 静态净收益，作为极差/平级奖基数
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @param now 本轮任务时间
	 * @param context 团队奖励收集上下文
	 */
	private void distributeDiffAndSameLevelReward(StakeHostingOrder order, List<ParentUserTaskVo> parentUsers,
												  BigDecimal grossReward, BigDecimal baseStaticRate,
												  BigDecimal afiAccelerateRate, BigDecimal actualStaticRate,
												  BigDecimal serviceFeeRatio, BigDecimal serviceFee,
												  BigDecimal netReward, int rewardDay, Date now,
												  TeamRewardCollectContext context) {
		// 1. 等级比例来自用户等级配置，单位是%；这里会在上下文中缓存，避免每笔订单重复查配置。
		Map<Integer, BigDecimal> levelRatioMap = getCachedLevelRatioMap(context);
		// 2. coveredRatio 表示上级链已发放到的最高覆盖比例，后续只发放更高等级的差额部分。
		BigDecimal coveredRatio = BigDecimal.ZERO;
		for (int i = 0; i < parentUsers.size(); i++) {
			ParentUserTaskVo parent = parentUsers.get(i);
			Integer level = effectiveLevel(parent);
			BigDecimal levelRatio = levelRatioMap.getOrDefault(level, BigDecimal.ZERO);
			if (levelRatio.compareTo(BigDecimal.ZERO) <= 0) {
				continue;
			}
			// 3. 极差比例 = 当前上级等级比例 - 已覆盖比例；没有正差额则不发放。
			BigDecimal diffRatio = levelRatio.subtract(coveredRatio);
			if (diffRatio.compareTo(BigDecimal.ZERO) > 0) {
				// 4. F5及以上需要向上收集同级组，用于后续平级奖；低等级按单人极差处理。
				SameLevelGroup sameLevelGroup = level >= 5
					? collectSameLevelGroupUntilHigher(parentUsers, i, level)
					: SameLevelGroup.single(i);
				BigDecimal diffRewardAmount = calculateReward(netReward, diffRatio);
				if (CollectionUtil.isNotEmpty(sameLevelGroup.sameIndexes)) {
					ParentUserTaskVo diffUser = parentUsers.get(sameLevelGroup.sameIndexes.get(0));
					List<Integer> rewardSameIndexes = sameLevelGroup.rewardSameIndexes();
					boolean hasLaterSameLevel = CollectionUtil.isNotEmpty(rewardSameIndexes);
					// 5. 有后续同级时，第一位同级用户只拿极差池一半，另一半作为平级池向上分配。
					BigDecimal firstDiffRewardAmount = hasLaterSameLevel
						? diffRewardAmount.divide(TWO, ConstantStatic.newScale, ConstantStatic.roundingModeNew)
						: diffRewardAmount;
					// 6. 第一位同级用户的奖励仍记录为极差奖，来源订单和比例保留在结算明细中。
					collectTeamReward(context, order, diffUser.getUserId(), REWARD_TYPE_DIFF, level, netReward, diffRatio,
						firstDiffRewardAmount, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
						serviceFeeRatio, serviceFee, netReward, rewardDay, now);
					// 7. 当前等级比例已经覆盖，后续上级只有等级比例更高时才继续拿差额。
					coveredRatio = levelRatio;
					if (level >= 5 && hasLaterSameLevel) {
						BigDecimal sameLevelPool = diffRewardAmount.subtract(firstDiffRewardAmount);
						// 8. 平级池只分给后续同级用户，第一位同级用户不再参与平级奖。
						collectSameLevelReward(order, parentUsers, rewardSameIndexes,
							level, netReward, diffRatio, sameLevelPool, grossReward, baseStaticRate,
							afiAccelerateRate, actualStaticRate, serviceFeeRatio, serviceFee, rewardDay, now, context);
					}
				}
				// 9. 同级组中间的低级别和同级用户已处理完，循环直接跳到下一位更高等级上级。
				i = sameLevelGroup.nextIndex - 1;
			}
		}
	}

	/**
	 * 收集后续同级用户的平级奖励。
	 *
	 * <p>sameIndexes 不包含第一位拿极差奖的同级用户，只包含后续同级用户。
	 * 平级池由第一位同级用户让出的极差池一半组成，后续同级按位置递减领取。</p>
	 *
	 * @param order 产生静态收益的托管订单
	 * @param parentUsers 有效上级链
	 * @param sameIndexes 后续同级用户在上级链中的下标
	 * @param level 平级奖励对应的有效等级
	 * @param netReward 静态净收益，作为奖励基数记录到结算明细
	 * @param diffRatio 触发本组同级奖励的极差比例，单位%
	 * @param sameLevelPool 平级奖励池金额
	 * @param grossReward 静态毛收益
	 * @param baseStaticRate 基础静态收益率乘数
	 * @param afiAccelerateRate AFI加速倍率
	 * @param actualStaticRate 实际静态收益率，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @param now 本轮任务时间
	 * @param context 团队奖励收集上下文
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
			// 每位平级用户都单独写钱包增量、奖励记录和结算明细，便于追踪来源订单和分配序号。
			collectTeamReward(context, order, rewardUser.getUserId(), REWARD_TYPE_SAME_LEVEL, level, netReward, diffRatio,
				sameLevelReward, grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
				serviceFeeRatio, serviceFee, netReward, rewardDay, now);
		}
	}

	/**
	 * 从当前上级开始收集同级组，直到遇到更高等级上级。
	 *
	 * <p>同级组用于F5及以上平级奖。扫描过程中低等级上级会被跨过，不参与当前同级组；
	 * 遇到更高等级上级时停止，让外层循环继续计算新的极差覆盖。</p>
	 *
	 * @param parentUsers 有效上级链
	 * @param startIndex 当前触发极差的上级下标
	 * @param level 当前极差等级
	 * @return 同级用户下标集合，以及外层循环下一次应继续扫描的位置
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
	 * 计算单个平级用户应领取的平级奖励金额。
	 *
	 * <p>公式：第n个后续同级用户领取 pool / 2^n；如果是最后一个同级用户，
	 * 当前实现使用 pool / 2^(总人数-1)。因此多个同级用户时不是平均分，而是按位置递减。</p>
	 *
	 * @param pool 平级奖励池金额
	 * @param sameIndex 当前用户在后续同级用户中的序号，从1开始
	 * @param sameCount 后续同级用户总数
	 * @return 当前平级用户应得金额
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
	 * 判断上级用户是否需要跳过动态奖励。
	 *
	 * <p>当前只校验是否具备有效托管奖励资格。返回 null 表示可发放；
	 * 返回跳过原因时，调用方可选择写未到账明细或直接跳过。</p>
	 *
	 * @param user 上级用户任务视图
	 * @return 跳过原因；null表示不跳过
	 */
	private Integer getRewardSkipReason(ParentUserTaskVo user) {
		if (!isValidStakeHostingRewardUser(user)) {
			return SKIP_NO_ACTIVE_ORDER;
		}
		return null;
	}

	/**
	 * 获取并缓存用户完整上级链。
	 *
	 * <p>同一轮101任务中，一个用户可能有多笔托管订单，缓存上级链可以避免重复查询。</p>
	 *
	 * @param userId 下级用户ID
	 * @param context 团队奖励收集上下文
	 * @return 上级链；无上级时返回空列表
	 */
	private List<ParentUserTaskVo> getCachedParentUsers(Long userId, TeamRewardCollectContext context) {
		if (userId == null) {
			return new ArrayList<>();
		}
		return context.parentUserCache.computeIfAbsent(userId, userInfoService::getParentUserTaskVo);
	}

	/**
	 * 获取并缓存可参与极差/平级奖励的有效上级链。
	 *
	 * <p>直推奖会先看直属上级并单独判断；极差/平级奖这里只保留具备托管奖励资格的上级，
	 * 无效上级不消耗覆盖比例。</p>
	 *
	 * @param userId 下级用户ID
	 * @param context 团队奖励收集上下文
	 * @return 已过滤有效资格的上级链
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
	 * 判断上级是否具备托管动态奖励资格。
	 *
	 * <p>ParentUserTaskVo.isValid 由上级链查询侧按业务资格计算，101 在这里直接使用该结果。
	 * false 表示没有有效托管资格，不发动态奖励。</p>
	 *
	 * @param user 上级用户任务视图
	 * @return true表示可参与托管动态奖励
	 */
	private boolean isValidStakeHostingRewardUser(ParentUserTaskVo user) {
		return user != null && user.getIsValid() != null && user.getIsValid() == 1;
	}

	/**
	 * 获取并缓存等级动态奖励比例。
	 *
	 * <p>等级比例本轮任务内稳定，缓存到上下文避免每笔订单重复查询等级配置。</p>
	 *
	 * @param context 团队奖励收集上下文
	 * @return key为有效等级，value为团队奖励比例，单位%
	 */
	private Map<Integer, BigDecimal> getCachedLevelRatioMap(TeamRewardCollectContext context) {
		if (context.levelRatioMap == null) {
			context.levelRatioMap = getLevelRatioMap();
		}
		return context.levelRatioMap;
	}

	/**
	 * 从等级配置表读取团队奖励比例。
	 *
	 * <p>比例单位是%，例如10表示可覆盖静态净收益的10%。等级0固定为0，避免无等级用户参与极差覆盖。</p>
	 *
	 * @return key为等级，value为团队奖励比例，单位%
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
	 * 获取并缓存直推奖励比例。
	 *
	 * <p>直推比例来自系统参数，本轮任务内复用同一份配置。</p>
	 *
	 * @param context 团队奖励收集上下文
	 * @return 直推奖励比例，单位%
	 */
	private BigDecimal getCachedDirectRewardRatioPercent(TeamRewardCollectContext context) {
		if (context.directRewardRatioPercent == null) {
			context.directRewardRatioPercent = getDirectRewardRatioPercent();
		}
		return context.directRewardRatioPercent;
	}

	/**
	 * 读取直推奖励比例系统参数。
	 *
	 * @return 直推奖励比例，单位%
	 */
	private BigDecimal getDirectRewardRatioPercent() {
		String value = sysParaServiceImpl.getValue(ConstantSys.biz_stake_hosting_direct_reward_ratio);
		if (StrUtil.isBlank(value)) {
			throw new ServiceException("Direct reward ratio is required");
		}
		return new BigDecimal(value);
	}

	/**
	 * 按百分比计算奖励金额。
	 *
	 * <p>公式：奖励金额 = 基数 * 比例 / 100。基数通常是静态净收益，
	 * 比例可能是直推比例、极差比例或平级沿用的极差比例。</p>
	 *
	 * @param baseAmount 奖励基数
	 * @param ratioPercent 奖励比例，单位%
	 * @return 按项目金额精度处理后的奖励金额
	 */
	private BigDecimal calculateReward(BigDecimal baseAmount, BigDecimal ratioPercent) {
		if (baseAmount == null || ratioPercent == null || baseAmount.compareTo(BigDecimal.ZERO) <= 0 || ratioPercent.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO;
		}
		return baseAmount.multiply(ratioPercent)
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	/**
	 * 计算上级任务视图的有效等级。
	 *
	 * <p>动态奖励按 gameLevel、minGameLevel、adminGameLevel 三者最大值作为有效等级，
	 * 避免某一种等级来源为空时影响奖励覆盖比例。</p>
	 *
	 * @param user 上级用户任务视图
	 * @return 有效等级，空等级按0处理
	 */
	private Integer effectiveLevel(ParentUserTaskVo user) {
		return Math.max(Math.max(defaultLevel(user.getGameLevel()), defaultLevel(user.getMinGameLevel())), defaultLevel(user.getAdminGameLevel()));
	}

	/**
	 * 计算用户实体的有效等级。
	 *
	 * @param user 用户实体
	 * @return 有效等级，空等级按0处理
	 */
	private int effectiveLevel(UserInfo user) {
		return Math.max(Math.max(defaultLevel(user.getGameLevel()), defaultLevel(user.getMinGameLevel())), defaultLevel(user.getAdminGameLevel()));
	}

	/**
	 * 将空等级兜底为0。
	 *
	 * @param level 原始等级
	 * @return 非空等级
	 */
	private int defaultLevel(Integer level) {
		return level == null ? 0 : level;
	}

	/**
	 * 收集团队动态奖励的钱包增量、奖励记录和结算明细。
	 *
	 * <p>用户购买订单按直推/极差/平级来源类型进入 `valid_num1`；后台拨付订单触发的动态奖励统一使用
	 * sourceType=48，并按订单收益分配方式决定进入上级用户 `valid_num3` 锁定USDT或 `valid_num1` 可用USDT。
	 * 后台拨付动态收益不累计到普通动态奖励汇总表。</p>
	 *
	 * @param context 团队奖励收集上下文
	 * @param order 静态收益来源订单
	 * @param receiveUserId 动态奖励接收用户ID
	 * @param rewardType 动态奖励类型：直推、极差或平级
	 * @param rewardLevel 接收用户有效等级
	 * @param rewardBase 奖励基数，通常为静态净收益
	 * @param ratioPercent 奖励比例，单位%
	 * @param rewardAmount 动态奖励金额
	 * @param grossReward 静态毛收益
	 * @param baseStaticRate 基础静态收益率乘数
	 * @param afiAccelerateRate AFI加速倍率
	 * @param actualStaticRate 实际静态收益率，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额
	 * @param netReward 静态净收益
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @param now 本轮任务时间
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
		boolean grantReward = isAdminGrantOrder(order);
		// 1. 按订单来源和奖励类型选择钱包流水 sourceType；后台拨付动态收益必须独立为48。
		int moneySourceType = grantReward ? ConstantType.user_money_log_source_type.type_48
			: rewardType == REWARD_TYPE_DIRECT ? ConstantType.user_money_log_source_type.type_32
			: rewardType == REWARD_TYPE_DIFF ? ConstantType.user_money_log_source_type.type_33
			: ConstantType.user_money_log_source_type.type_34;
		// 2. 奖励记录 sourceType 与钱包 sourceType 对齐，方便后台按真实收益/拨付收益拆分统计。
		int rewardSourceType = grantReward ? ConstantType.xms_reward_record_source_type.type_48
			: rewardType == REWARD_TYPE_DIRECT ? ConstantType.xms_reward_record_source_type.type_28
			: rewardType == REWARD_TYPE_DIFF ? ConstantType.xms_reward_record_source_type.type_29
			: ConstantType.xms_reward_record_source_type.type_30;
		String gtId = IDUtils.getSnowflakeStr();
		boolean grantDynamicRewardToAvailable = grantReward && isGrantOrderDynamicRewardToAvailable(order);
		// 用户购买单动态奖励进入可用USDT；后台拨付单动态奖励按订单收益分配方式进入可用或锁定USDT。
		UserMoney userMoney = new UserMoney();
		userMoney.setId(receiveUserId);
		if (grantReward && !grantDynamicRewardToAvailable) {
			userMoney.setValidNum3(rewardAmount);
		} else {
			userMoney.setValidNum1(rewardAmount);
		}
		userMoney.setGtId(gtId);
		userMoney.setSourceCode(order.getOrderNo());
		userMoney.setSourceId(order.getUserId());
		userMoney.setSourceType(moneySourceType);
		userMoney.setUpdateTime(now);
		context.userMoneyList.add(userMoney);

		// 奖励记录使用独立来源类型，coinType 与最终入账资产字段保持一致。
		RewardRecord rewardRecord = new RewardRecord();
		rewardRecord.setOrderCode(IDUtils.getSnowflakeStr());
		rewardRecord.setUserId(receiveUserId);
		rewardRecord.setAmount(rewardAmount);
		rewardRecord.setCoinType(grantReward && !grantDynamicRewardToAvailable
			? ConstantType.user_money_coin_type.type_3
			: ConstantType.user_money_coin_type.type_1);
		rewardRecord.setSourceType(rewardSourceType);
		rewardRecord.setSourceOrderCode(order.getOrderNo());
		rewardRecord.setSourceUserId(order.getUserId());
		rewardRecord.setGtId(gtId);
		rewardRecord.setCreateTime(now);
		context.rewardRecordList.add(rewardRecord);
		if (!grantReward) {
			collectTeamRewardSummary(context, receiveUserId, rewardType, rewardAmount);
		}
		context.settlementList.add(buildSettlement(order, receiveUserId, rewardType, rewardLevel, rewardBase, ratioPercent, rewardAmount,
			grossReward, baseStaticRate, afiAccelerateRate, actualStaticRate,
			serviceFeeRatio, serviceFee, netReward, ARRIVAL_YES, null, rewardDay, now));
	}

	/**
	 * 判断后台拨付订单动态收益是否进入可用USDT。
	 *
	 * @param order 静态收益来源订单
	 * @return true 表示 mode=2，后台拨付动态收益进入 `valid_num1`
	 */
	private boolean isGrantOrderDynamicRewardToAvailable(StakeHostingOrder order) {
		return order != null
			&& order.getGrantRewardMode() != null
			&& order.getGrantRewardMode() == StakeHostingOrderServiceImpl.GRANT_REWARD_MODE_DYNAMIC_AVAILABLE;
	}

	/**
	 * 收集未到账团队奖励结算明细。
	 *
	 * <p>该方法只写结算上下文，不发钱包。当前直推无效上级逻辑暂未调用，
	 * 保留用于后续需要展示跳过原因时复用。</p>
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
	 * 汇总用户购买单的极差和平级奖励统计。
	 *
	 * <p>后台拨付订单动态收益按订单收益分配方式进入可用或锁定USDT，不计入普通团队奖励汇总；
	 * 因此调用方只在非后台拨付订单时调用本方法。</p>
	 *
	 * @param context 团队奖励收集上下文
	 * @param receiveUserId 收益接收用户ID
	 * @param rewardType 动态奖励类型
	 * @param rewardAmount 奖励金额
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
	 * 批量落地团队动态奖励上下文。
	 *
	 * <p>该方法统一 flush 钱包增量、奖励记录、结算明细和用户奖励汇总。
	 * 任一批量钱包更新失败都会抛异常回滚101任务事务。</p>
	 *
	 * @param context 团队奖励收集上下文
	 */
	private void flushTeamRewardContext(TeamRewardCollectContext context) {
		if (context == null || context.isEmpty()) {
			return;
		}
		if (CollectionUtil.isNotEmpty(context.userMoneyList)) {
			// 团队动态奖励按来源订单和后台拨付模式分流到 valid_num1 或 valid_num3。
			batchUpdateRewardMoney(context.userMoneyList);
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
	 * 按钱包资产字段批量发放托管奖励。
	 *
	 * <p>真实用户购买托管收益写入 `valid_num1`；后台拨付托管静态收益写入 `valid_num3`，
	 * 后台拨付动态收益按订单收益分配方式写入 `valid_num3` 或 `valid_num1`。调用方必须提前设置好金额字段、
	 * sourceType、sourceCode 和 gtId。</p>
	 *
	 * @param userMoneyList 待批量入账的钱包增量
	 */
	private void batchUpdateRewardMoney(List<UserMoney> userMoneyList) {
		if (CollectionUtil.isEmpty(userMoneyList)) {
			return;
		}
		// 先按目标资产字段拆分，避免同一条批量SQL同时承担真实收益和拨付收益两种资产语义。
		List<UserMoney> validNum1List = userMoneyList.stream()
			.filter(item -> item.getValidNum1() != null && item.getValidNum1().compareTo(BigDecimal.ZERO) != 0)
			.collect(Collectors.toList());
		List<UserMoney> validNum3List = userMoneyList.stream()
			.filter(item -> item.getValidNum3() != null && item.getValidNum3().compareTo(BigDecimal.ZERO) != 0)
			.collect(Collectors.toList());
		// valid_num1 写可用USDT，valid_num3 写锁定USDT，两边都带 gtId/sourceCode/sourceType/sourceId 供流水追踪。
		batchUpdateMoneyValid1(validNum1List);
		batchUpdateMoneyValid3(validNum3List);
	}

	/**
	 * 批量增加用户锁定USDT余额。
	 *
	 * @param userMoneyList 待写入 `valid_num3` 的钱包增量
	 */
	private void batchUpdateMoneyValid3(List<UserMoney> userMoneyList) {
		if (CollectionUtil.isEmpty(userMoneyList)) {
			return;
		}
		int[] rows = jdbcTemplate.batchUpdate(SQL_VALID_NUM3, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				UserMoney userMoney = userMoneyList.get(i);
				ps.setTimestamp(1, new java.sql.Timestamp(userMoney.getUpdateTime().getTime()));
				ps.setString(2, userMoney.getGtId());
				ps.setBigDecimal(3, userMoney.getValidNum3());
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
			log.error("101 grant reward USDT batch update failed");
			throw new ServiceException("101 grant reward USDT batch update failed");
		}
	}

	/**
	 * 批量增加用户可用USDT余额。
	 *
	 * @param userMoneyList 待写入 `valid_num1` 的钱包增量
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
	 * 团队动态奖励批量收集上下文。
	 *
	 * <p>101 在订单循环中只收集钱包增量、奖励记录、结算明细和汇总增量，最后统一批量落库。
	 * 同时缓存上级链、等级比例和直推比例，减少同一轮任务内的重复查询。</p>
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
		 * 判断当前上下文是否没有任何待落地数据。
		 *
		 * @return true 表示无需 flush
		 */
		private boolean isEmpty() {
			return userMoneyList.isEmpty() && rewardRecordList.isEmpty() && settlementList.isEmpty() && summaryMap.isEmpty();
		}
	}

	/**
	 * 同级奖励扫描结果。
	 *
	 * <p>sameIndexes 保存当前同级组的有效上级下标；nextIndex 表示遇到更高等级后，
	 * 外层极差循环下一次应该继续扫描的位置。</p>
	 */
	private static class SameLevelGroup {
		private final List<Integer> sameIndexes;
		private final int nextIndex;

		private SameLevelGroup(List<Integer> sameIndexes, int nextIndex) {
			this.sameIndexes = sameIndexes;
			this.nextIndex = nextIndex;
		}

		/**
		 * 获取真正领取平级奖的后续同级用户下标。
		 *
		 * <p>同级组第一位用户已领取极差奖，不再参与平级奖。</p>
		 *
		 * @return 后续同级用户下标集合
		 */
		private List<Integer> rewardSameIndexes() {
			if (sameIndexes.size() <= 1) {
				return new ArrayList<>();
			}
			return new ArrayList<>(sameIndexes.subList(1, sameIndexes.size()));
		}

		/**
		 * 构造无平级组的单人极差结果。
		 *
		 * @param index 当前上级下标
		 * @return 只包含当前用户的同级组
		 */
		private static SameLevelGroup single(int index) {
			List<Integer> indexes = new ArrayList<>();
			indexes.add(index);
			return new SameLevelGroup(indexes, index + 1);
		}
	}

	/**
	 * 立即保存一条托管收益结算明细。
	 *
	 * <p>101 当前主链路大多使用批量收集保存，本方法保留给需要单条写入的补偿或调试链路。
	 * 参数会透传到 buildSettlement，包含来源订单、接收用户、奖励类型、比例、金额和跳过原因。</p>
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
	 * 构建托管收益结算明细对象。
	 *
	 * <p>结算明细是解释101收益发放的核心审计记录：静态收益写来源订单和服务费信息；
	 * 直推、极差、平级奖励额外写接收用户、奖励等级、奖励比例、到账状态和跳过原因。</p>
	 *
	 * @param order 静态收益来源订单
	 * @param receiveUserId 奖励接收用户；静态服务费明细可为空
	 * @param rewardType 奖励类型
	 * @param rewardLevel 接收用户有效等级
	 * @param rewardBase 奖励基数
	 * @param ratioPercent 奖励比例，单位%
	 * @param rewardAmount 奖励金额或服务费金额
	 * @param grossReward 静态毛收益
	 * @param baseStaticRate 基础静态收益率，单位%
	 * @param afiAccelerateRate AFI加速倍率
	 * @param actualStaticRate 实际静态收益率，单位%
	 * @param serviceFeeRatio 服务费比例，单位%
	 * @param serviceFee 服务费金额
	 * @param netReward 静态净收益
	 * @param arrivalStatus 到账状态
	 * @param skipReason 跳过原因；正常到账时为空
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @param now 创建时间
	 * @return 待保存的托管收益结算明细
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
