package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantSys;
import com.xms.common.constant.Constants;
import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.StakeHostingDailyTeamPerformance;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserRelation;
import com.xms.dao.mapper.StakeHostingDailyTeamPerformanceMapper;
import com.xms.dao.mapper.StakeHostingOrderMapper;
import com.xms.dao.service.*;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 托管G7每日团队业绩与收益率快照Service业务层处理。
 *
 * @author xms
 */
@Slf4j
@Service
@AllArgsConstructor
public class StakeHostingDailyTeamPerformanceServiceImpl
	extends XmsDataServiceImpl<StakeHostingDailyTeamPerformanceMapper, StakeHostingDailyTeamPerformance>
	implements IStakeHostingDailyTeamPerformanceService {

	private static final int G7_STATUS_WAIT = 0;
	private static final int G7_STATUS_DONE = 1;
	private static final int CALC_STATUS_DONE = 1;
	private static final int RATE_SOURCE_G7 = 1;
	private static final int RATE_SOURCE_PURE_STATIC = 3;
	private static final BigDecimal LOW_BASE = new BigDecimal("100");
	private static final BigDecimal MAX_G_DAY = new BigDecimal("200");
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final BigDecimal HUNDRED = new BigDecimal("100");

	private final StakeHostingOrderMapper stakeHostingOrderMapper;
	private final UserInfoService userInfoService;
	private final UserRelationService userRelationService;
	private final IStakeHostingStaticRateConfigService staticRateConfigService;
	private final ISysParaService iSysParaService;
	private final Environment environment;

	/**
	 * 查询后台G7每日快照列表。
	 *
	 * <p>该方法只用于后台展示和导出每日团队业绩、G_day、Gsmooth和命中静态收益率，
	 * 不触发快照重算，也不修改收益率结果。</p>
	 *
	 * @param performance 查询条件
	 * @return G7每日快照列表
	 */
	@Override
	public List<StakeHostingDailyTeamPerformance> selectStakeHostingDailyTeamPerformanceList(StakeHostingDailyTeamPerformance performance) {
		return baseMapper.selectStakeHostingDailyTeamPerformanceList(performance);
	}

	/**
	 * 记录托管订单生效当天给有效上级带来的G7团队新增业绩。
	 *
	 * <p>该方法通常由托管订单生效后的异步流程触发。它先用订单的
	 * g7_new_performance_status 做幂等抢占，成功后才把订单托管USDT金额累计到买家所有有效上级的当天快照中。</p>
	 *
	 * @param orderId 已生效托管订单ID
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recordOrderTeamNewAmount(Long orderId) {
		StakeHostingOrder order = getOrder(orderId);
		if (order.getEffectiveTime() == null) {
			throw new ServiceException("G7团队新增统计失败，订单生效时间为空");
		}
		boolean locked = stakeHostingOrderMapper.update(null, new LambdaUpdateWrapper<StakeHostingOrder>()
			.eq(StakeHostingOrder::getId, orderId)
			.eq(StakeHostingOrder::getG7NewPerformanceStatus, G7_STATUS_WAIT)
			.set(StakeHostingOrder::getG7NewPerformanceStatus, G7_STATUS_DONE)
			.set(StakeHostingOrder::getG7NewPerformanceTime, new Date())
			.set(StakeHostingOrder::getUpdateTime, new Date())) > 0;
		if (!locked) {
			log.info("G7团队新增统计跳过，订单已处理 orderId={}", orderId);
			return;
		}
		recordParentAmount(order.getUserId(), statDay(order.getEffectiveTime()), order.getStakeUsdtAmount(), true);
	}

	/**
	 * 记录托管订单到期后的G7团队到期业绩。
	 *
	 * <p>当前101静态日利率不再使用到期金额扣减，该方法仅为兼容历史字段和旧调用保留。
	 * 方法仍使用订单 g7_expire_performance_status 做幂等抢占。</p>
	 *
	 * @param orderId 到期托管订单ID
	 * @param rewardDay 到期收益日，格式yyyyMMdd
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@Deprecated
	public void recordOrderTeamExpiredAmountNextDay(Long orderId, Integer rewardDay) {
		StakeHostingOrder order = getOrder(orderId);
		boolean locked = stakeHostingOrderMapper.update(null, new LambdaUpdateWrapper<StakeHostingOrder>()
			.eq(StakeHostingOrder::getId, orderId)
			.eq(StakeHostingOrder::getG7ExpirePerformanceStatus, G7_STATUS_WAIT)
			.set(StakeHostingOrder::getG7ExpirePerformanceStatus, G7_STATUS_DONE)
			.set(StakeHostingOrder::getG7ExpirePerformanceTime, new Date())
			.set(StakeHostingOrder::getUpdateTime, new Date())) > 0;
		if (!locked) {
			log.info("G7团队到期统计跳过，订单已处理 orderId={}", orderId);
			return;
		}
		recordParentAmount(order.getUserId(), nextDay(rewardDay), order.getStakeUsdtAmount(), false);
	}

	/**
	 * 准备101收益日所需的G7收益率快照。
	 *
	 * <p>该方法不发放收益、不改钱包，只负责把当天参与101的用户和当天已有团队新增统计的用户合并，
	 * 然后按用户生成或补齐 `t_stake_hosting_daily_team_performance` 快照。101 后续会读取
	 * calc_status=1 的快照作为静态收益率来源之一。</p>
	 *
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @param rewardUserIds 本轮101待发放订单所属用户ID集合
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void prepareDailySnapshots(Integer rewardDay, List<Long> rewardUserIds) {
		moveG7SnapshotsBackOneDayForTest();
		// 1. 合并当天审计记录、本轮101订单用户和G7总业绩窗口用户，确保收益率快照不因当天无事件而断档。
		Set<Long> userIds = new LinkedHashSet<>();
		List<Long> statUserIds = baseMapper.selectUserIdsByStatDay(rewardDay);
		if (CollectionUtil.isNotEmpty(statUserIds)) {
			userIds.addAll(statUserIds);
		}
		if (CollectionUtil.isNotEmpty(rewardUserIds)) {
			userIds.addAll(rewardUserIds);
		}
		// 1.1 G7总业绩TVL是存量状态指标，当前仍有团队总业绩的用户即使当天无购买事件也要生成快照。
		List<Long> umbrellaPerformanceUserIds = baseMapper.selectUserIdsWithUmbrellaPerformance();
		if (CollectionUtil.isNotEmpty(umbrellaPerformanceUserIds)) {
			userIds.addAll(umbrellaPerformanceUserIds);
		}
		// 1.2 昨日有团队总业绩的用户也要纳入，用于捕捉今天归零或下降产生的负G_day。
		List<Long> yesterdayTeamTotalUserIds = baseMapper.selectUserIdsWithYesterdayTeamTotalPerformance(previousDay(rewardDay));
		if (CollectionUtil.isNotEmpty(yesterdayTeamTotalUserIds)) {
			userIds.addAll(yesterdayTeamTotalUserIds);
		}
		// 1.3 最近6天发生过真实G7事件的用户也要纳入，用于团队总业绩归零后继续完成7天平滑窗口。
		List<Long> recentG7EventUserIds = baseMapper.selectUserIdsWithRecentG7Event(beforeDays(rewardDay, 6), rewardDay);
		if (CollectionUtil.isNotEmpty(recentG7EventUserIds)) {
			userIds.addAll(recentG7EventUserIds);
		}
		// 2. 未命中G7窗口时，快照先保存回本前纯静态比例；真实发放还会按订单是否回本重新选择纯静态参数。
		BigDecimal pureStaticRateBeforeReturnPercent = new BigDecimal(iSysParaService.getValue(ConstantSys.PURE_STATIC_RATE_BEFORE_RETURN_PERCENT));
		// 3. 批量预加载昨日团队新增审计值、昨日团队总业绩和最近G_day历史，避免循环内产生N+1查询。
		Map<Long, BigDecimal> yesterdayTeamNewMap = loadYesterdayTeamNewAmountMap(userIds, rewardDay);
		Map<Long, BigDecimal> yesterdayTeamTotalPerformanceMap = loadYesterdayTeamTotalPerformanceMap(userIds, rewardDay);
		// 3.1 批量查这些用户 rewardDay 之前最多 6 天的已计算快照，用于提取历史 g_day，计算 g_smooth。
		Map<Long, List<StakeHostingDailyTeamPerformance>> recentSnapshotMap = loadRecentSnapshotMap(userIds, rewardDay);
		// 4. 逐个用户生成当天快照；已计算完成的快照会在 prepareOneSnapshot 中跳过，支持任务重跑。
		for (Long userId : userIds) {
			prepareOneSnapshot(userId, rewardDay, yesterdayTeamNewMap.get(userId),
				yesterdayTeamTotalPerformanceMap.get(userId), recentSnapshotMap.get(userId),
				pureStaticRateBeforeReturnPercent);
		}
	}

	/**
	 * 测试环境按开关将G7历史快照整体往前移动一天。
	 *
	 * <p>该方法只在非 prod 且系统参数 {@code kaixxguanxx=1} 时执行，用于本地/测试环境模拟连续多天G7历史数据。
	 * 它只移动今天以前记录的 `stat_day` 和 `update_time`，不重算 G7快照金额、g_day、g_smooth、base_static_rate、rate_source，
	 * 也不修改 `calc_status`。移动顺序按 `user_id + stat_day` 升序，尽量避开 `uk_user_day(user_id, stat_day)` 唯一索引冲突。</p>
	 */
	private void moveG7SnapshotsBackOneDayForTest() {
		String profile = environment.getProperty(Constants.ACTIVE_PROFILES_PROPERTY);
		if (Constants.ACTIVE_PROPERTY_PROD.equalsIgnoreCase(profile)) {
			return;
		}
		String flag = iSysParaService.getValue(ConstantSys.kaixxguanxx);
		if (!"1".equals(flag)) {
			return;
		}
		Integer today = statDay(new Date());
		List<StakeHostingDailyTeamPerformance> rows = lambdaQuery()
			.lt(StakeHostingDailyTeamPerformance::getStatDay, today)
			.eq(StakeHostingDailyTeamPerformance::getDeleted, 0)
			.orderByAsc(StakeHostingDailyTeamPerformance::getUserId)
			.orderByAsc(StakeHostingDailyTeamPerformance::getStatDay)
			.orderByAsc(StakeHostingDailyTeamPerformance::getId)
			.list();
		if (CollectionUtil.isEmpty(rows)) {
			log.info("G7测试快照日期回退跳过，没有今天以前的快照记录 profile={}, today={}", profile, today);
			return;
		}
		Date now = new Date();
		for (StakeHostingDailyTeamPerformance row : rows) {
			Integer newStatDay = previousDay(row.getStatDay());
			boolean updated = lambdaUpdate()
				.eq(StakeHostingDailyTeamPerformance::getId, row.getId())
				.set(StakeHostingDailyTeamPerformance::getStatDay, newStatDay)
				.set(StakeHostingDailyTeamPerformance::getUpdateTime, now)
				.update();
			if (!updated) {
				throw new ServiceException("G7测试快照日期回退失败，id=" + row.getId());
			}
		}
		log.info("G7测试快照日期回退完成 profile={}, today={}, count={}", profile, today, rows.size());
	}

	/**
	 * 查询用户某天已经计算完成的G7快照。
	 *
	 * @param userId 用户ID
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return 已计算完成且未删除的快照；不存在时返回null
	 */
	@Override
	public StakeHostingDailyTeamPerformance getCalculatedSnapshot(Long userId, Integer rewardDay) {
		if (userId == null || rewardDay == null) {
			return null;
		}
		return lambdaQuery()
			.eq(StakeHostingDailyTeamPerformance::getUserId, userId)
			.eq(StakeHostingDailyTeamPerformance::getStatDay, rewardDay)
			.eq(StakeHostingDailyTeamPerformance::getCalcStatus, CALC_STATUS_DONE)
			.eq(StakeHostingDailyTeamPerformance::getDeleted, 0)
			.one();
	}

	/**
	 * 判断用户在指定日期是否存在可用于G7判断的团队总业绩窗口。
	 *
	 * @param userId 用户ID
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return true表示昨日或今日团队总业绩不全为0
	 */
	@Override
	public boolean hasTeamTvl(Long userId, Integer rewardDay) {
		StakeHostingDailyTeamPerformance snapshot = getCalculatedSnapshot(userId, rewardDay);
		return hasG7TeamTotalPerformanceSnapshot(snapshot);
	}

	/**
	 * 给买家的所有有效上级记录G7当天团队新增或到期USDT金额。
	 *
	 * <p>团队新增金额只作为审计字段和当天受影响用户标记。G7静态日利率改用
	 * t_user_info.umbrella_performance 保存的团队总业绩；到期金额方法保留是为了兼容旧字段和旧调用。</p>
	 *
	 * @param buyerUserId 买家用户ID
	 * @param statDay 统计日期，格式yyyyMMdd
	 * @param amount 托管USDT金额
	 * @param newAmount true表示新增，false表示到期
	 */
	private void recordParentAmount(Long buyerUserId, Integer statDay, BigDecimal amount, boolean newAmount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		List<UserRelation> parents = userRelationService.lambdaQuery()
			.eq(UserRelation::getPosUserId, buyerUserId)
			.eq(UserRelation::getActiveFlag, 1)
			.gt(UserRelation::getDistance, 0)
			.orderByAsc(UserRelation::getDistance)
			.list();
		if (CollectionUtil.isEmpty(parents)) {
			log.info("G7团队业绩统计跳过，买家无上级 buyerUserId={}", buyerUserId);
			return;
		}
		List<Long> parentIds = parents.stream().map(UserRelation::getParUserId).collect(Collectors.toList());
		List<UserInfo> users = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, parentIds)
			.eq(UserInfo::getDeleted, 0)
			.list();
		for (UserInfo user : users) {
			if (newAmount) {
				baseMapper.upsertTeamNewAmount(user.getUserId(), user.getAccount(), statDay, amount);
			} else {
				baseMapper.upsertTeamExpiredAmount(user.getUserId(), user.getAccount(), statDay, amount);
			}
		}
	}

	/**
	 * 生成单个用户某天的G7团队总业绩对比快照。
	 *
	 * <p>previous_team_total_performance 保存昨日团队总业绩，current_team_total_performance 保存当前
	 * t_user_info.umbrella_performance。previous_team_tvl/current_team_tvl 仍保留昨日/今日团队新增审计值，
	 * 不再参与 G_day 公式。</p>
	 *
	 * @param userId 用户ID
	 * @param rewardDay 收益日，格式yyyyMMdd
	 */
	private void prepareOneSnapshot(Long userId, Integer rewardDay, BigDecimal yesterdayTeamNewAmount,
									BigDecimal yesterdayTeamTotalPerformance,
									List<StakeHostingDailyTeamPerformance> previousSnapshots,
									BigDecimal pureStaticRateBeforeReturnPercent) {
		UserInfo user = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		baseMapper.upsertEmptyDay(user.getUserId(), user.getAccount(), rewardDay);
		StakeHostingDailyTeamPerformance snapshot = lambdaQuery()
			.eq(StakeHostingDailyTeamPerformance::getUserId, userId)
			.eq(StakeHostingDailyTeamPerformance::getStatDay, rewardDay)
			.one();
		if (snapshot == null || (snapshot.getCalcStatus() != null && snapshot.getCalcStatus() == CALC_STATUS_DONE)) {
			return;
		}
		// 旧字段保留为审计：previous/current_team_tvl 继续存昨日/今日团队新增，不参与G7公式。
		BigDecimal previousTeamNewAmount = nvl(yesterdayTeamNewAmount)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal currentTeamNewAmount = nvl(snapshot.getTeamNewAmount())
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 新G7口径：今日团队总业绩读取用户当前 umbrella_performance，昨日团队总业绩读取昨日快照。
		BigDecimal previousTeamTotalPerformance = nvl(yesterdayTeamTotalPerformance)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal currentTeamTotalPerformance = nvl(user.getUmbrellaPerformance())
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 单日G值按团队总业绩增长率计算：0->有业绩继续按低基数保护计算并封顶200%。
		BigDecimal gDay = calculateGDay(previousTeamTotalPerformance, currentTeamTotalPerformance);
		// 历史G值取 rewardDay 前最近最多6个已计算快照，用于和当天G值一起做最多7天平滑。
		List<BigDecimal> previousGDays = extractGDays(previousSnapshots);
		// G_smooth = 当天g_day + 最近最多6天历史g_day 的平均值；历史不足6天时按实际条数平均。
		BigDecimal gSmooth = calculateGSmooth(previousGDays, gDay);
		// 是否进入G7区间看今日/昨日团队总业绩，也看最近6天是否仍存在总业绩窗口。
		boolean hasG7Window = hasG7Window(previousTeamTotalPerformance, currentTeamTotalPerformance, previousSnapshots);
		// 命中G7窗口时用Gsmooth匹配G7收益率配置；未命中时先写回本前纯静态比例作为快照展示值。
		// 真实发放时 rate_source=3 会在 calculateStaticRate 内按订单是否回本重新选择纯静态参数。
		BigDecimal staticRate = hasG7Window ? staticRateConfigService.matchStaticRate(gSmooth) : pureStaticRateBeforeReturnPercent;
		// rate_source=1 表示G7区间收益率；rate_source=3 表示未推广/纯静态规则，不是“第3档收益率”。
		Integer rateSource = hasG7Window ? RATE_SOURCE_G7 : RATE_SOURCE_PURE_STATIC;
		// 将本次快照计算结果一次性写回，calc_status=1 表示该用户 rewardDay 的收益率快照已可被101读取。
		lambdaUpdate()
			.eq(StakeHostingDailyTeamPerformance::getId, snapshot.getId())
			.set(StakeHostingDailyTeamPerformance::getPreviousTeamTvl, previousTeamNewAmount)
			.set(StakeHostingDailyTeamPerformance::getCurrentTeamTvl, currentTeamNewAmount)
			.set(StakeHostingDailyTeamPerformance::getPreviousTeamTotalPerformance, previousTeamTotalPerformance)
			.set(StakeHostingDailyTeamPerformance::getCurrentTeamTotalPerformance, currentTeamTotalPerformance)
			.set(StakeHostingDailyTeamPerformance::getGDay, gDay)
			.set(StakeHostingDailyTeamPerformance::getGSmooth, gSmooth)
			.set(StakeHostingDailyTeamPerformance::getBaseStaticRate, staticRate)
			.set(StakeHostingDailyTeamPerformance::getRateSource, rateSource)
			.set(StakeHostingDailyTeamPerformance::getCalcStatus, CALC_STATUS_DONE)
			.set(StakeHostingDailyTeamPerformance::getUpdateTime, new Date())
			.update();
	}

	/**
	 * 批量加载待计算用户的昨日团队新增审计金额。
	 *
	 * <p>该值只用于回填 previous_team_tvl 审计字段，不参与新 G_day 公式。这里在外层一次性查询昨日记录，
	 * 避免prepareOneSnapshot按用户循环时逐个查询昨日快照。</p>
	 *
	 * @param userIds 本次需要准备G7快照的用户ID集合
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return key为用户ID，value为昨日团队新增托管USDT金额
	 */
	private Map<Long, BigDecimal> loadYesterdayTeamNewAmountMap(Set<Long> userIds, Integer rewardDay) {
		if (CollectionUtil.isEmpty(userIds)) {
			return java.util.Collections.emptyMap();
		}
		return baseMapper.selectByUserIdsAndStatDay(userIds, previousDay(rewardDay)).stream()
			.collect(Collectors.toMap(StakeHostingDailyTeamPerformance::getUserId,
				item -> nvl(item.getTeamNewAmount()).setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew),
				(left, right) -> left));
	}

	/**
	 * 批量加载待计算用户的昨日团队总业绩。
	 *
	 * <p>G_day的新分母来自昨日团队总业绩。这里读取昨日快照的 current_team_total_performance；
	 * 如果昨日没有快照，则调用方按0处理。</p>
	 *
	 * @param userIds 本次需要准备G7快照的用户ID集合
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return key为用户ID，value为昨日团队总业绩(质押量)
	 */
	private Map<Long, BigDecimal> loadYesterdayTeamTotalPerformanceMap(Set<Long> userIds, Integer rewardDay) {
		if (CollectionUtil.isEmpty(userIds)) {
			return java.util.Collections.emptyMap();
		}
		return baseMapper.selectByUserIdsAndStatDay(userIds, previousDay(rewardDay)).stream()
			.filter(item -> Integer.valueOf(CALC_STATUS_DONE).equals(item.getCalcStatus()))
			.collect(Collectors.toMap(StakeHostingDailyTeamPerformance::getUserId,
				item -> nvl(item.getCurrentTeamTotalPerformance()).setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew),
				(left, right) -> left));
	}

	/**
	 * 批量加载待计算用户的历史G_day。
	 *
	 * <p>prepareDailySnapshots会按用户循环生成当天快照。Gsmooth需要最近最多6天历史G_day，
	 * 如果在循环里逐个查询会形成N+1查询，所以这里在外层一次性查出并按用户ID分组。</p>
	 *
	 * @param userIds 本次需要准备G7快照的用户ID集合
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @return key为用户ID，value为该用户收益日前最近最多6条G_day，按日期倒序
	 */
	private Map<Long, List<StakeHostingDailyTeamPerformance>> loadRecentSnapshotMap(Set<Long> userIds, Integer rewardDay) {
		if (CollectionUtil.isEmpty(userIds)) {
			return java.util.Collections.emptyMap();
		}
		return baseMapper.selectRecentGDayBeforeBatch(userIds, rewardDay, beforeDays(rewardDay, 6)).stream()
			.collect(Collectors.groupingBy(StakeHostingDailyTeamPerformance::getUserId));
	}

	/**
	 * 按G7规则计算单日团队总业绩增长率。
	 *
	 * <p>本方法比较“今日团队总业绩”和“昨日团队总业绩”。昨日和今日总业绩都为0时按0%处理；
	 * previous=0且current大于0时继续使用 max(previous, 100) 作为低基数保护分母，并按200%封顶。
	 * 正向增长最高封顶200%，负增长不做下限截断。</p>
	 *
	 * @param previousTvl 昨日团队总业绩(质押量)
	 * @param currentTvl 今日团队总业绩(质押量)
	 * @return 单日增长率，单位%
	 */
	private BigDecimal calculateGDay(BigDecimal previousTvl, BigDecimal currentTvl) {
		// 业绩金额允许上游没有记录，统一按0处理，避免后续BigDecimal计算空指针。
		previousTvl = nvl(previousTvl);
		currentTvl = nvl(currentTvl);
		// 昨日和今日团队总业绩都为0时，本日G_day固定为0%。
		if (previousTvl.compareTo(BigDecimal.ZERO) == 0 && currentTvl.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
		}
		// 低基数保护：昨日总业绩小于100U时，分母按100U计算，包含0->有业绩的场景。
		BigDecimal denominator = previousTvl.max(LOW_BASE);
		// G_day = (今日总业绩 - 昨日总业绩) / max(昨日总业绩, 100) * 100%，结果单位是百分比。
		BigDecimal gDay = currentTvl.subtract(previousTvl).multiply(HUNDRED)
			.divide(denominator, 4, RoundingMode.HALF_UP);
		// 正向增长最多按200%参与后续Gsmooth和收益率档位匹配，负数保持原值体现业绩下滑。
		return gDay.compareTo(MAX_G_DAY) > 0 ? MAX_G_DAY.setScale(4, RoundingMode.HALF_UP) : gDay;
	}

	/**
	 * 判断快照是否存在可用于G7的团队总业绩窗口。
	 *
	 * <p>今日总业绩为0但昨日总业绩大于0时也属于有效G7快照，因为这会产生负增长；
	 * 只有昨日和今日总业绩都为0时，才需要结合历史窗口判断是否走未推广纯静态规则。</p>
	 *
	 * @param snapshot G7每日快照
	 * @return true表示应按G7快照收益率计算
	 */
	private boolean hasG7TeamTotalPerformanceSnapshot(StakeHostingDailyTeamPerformance snapshot) {
		if (snapshot == null) {
			return false;
		}
		return hasG7TeamTotalPerformance(snapshot.getPreviousTeamTotalPerformance(), snapshot.getCurrentTeamTotalPerformance());
	}

	/**
	 * 判断昨日或今日是否存在可用于G7计算的团队总业绩。
	 *
	 * <p>昨日和今日团队总业绩都为0时，实际静态收益发放需要继续看最近6天历史窗口；
	 * 如果历史窗口也为空，才按未推广规则处理。</p>
	 *
	 * @param previousTvl 昨日团队总业绩(质押量)
	 * @param currentTvl 今日团队总业绩(质押量)
	 * @return true表示应按G7区间收益率展示和计算
	 */
	private boolean hasG7TeamTotalPerformance(BigDecimal previousTvl, BigDecimal currentTvl) {
		return nvl(previousTvl).compareTo(BigDecimal.ZERO) > 0
			|| nvl(currentTvl).compareTo(BigDecimal.ZERO) > 0;
	}

	/**
	 * 判断用户是否仍处于最近最多7天G7平滑收益率窗口。
	 *
	 * <p>当天或昨日存在团队总业绩时，当前快照自然按G7区间计算；当天为0->0时，只要前6条历史G_day仍存在，
	 * 也继续使用当天G_day加历史G_day计算出的Gsmooth匹配区间，避免负增长尚未滚出窗口就回退到未推广0.5%。</p>
	 *
	 * @param previousTvl 昨日团队总业绩(质押量)
	 * @param currentTvl 今日团队总业绩(质押量)
	 * @param previousSnapshots 收益日前最近6个自然日内最多6条已计算快照
	 * @return true表示按Gsmooth命中G7区间，false表示最近窗口无G7记录，按未推广规则处理
	 */
	private boolean hasG7Window(BigDecimal previousTvl, BigDecimal currentTvl, List<StakeHostingDailyTeamPerformance> previousSnapshots) {
		return hasG7TeamTotalPerformance(previousTvl, currentTvl) || hasG7TeamTotalPerformanceHistory(previousSnapshots);
	}

	/**
	 * 判断最近6个自然日历史快照中是否出现过团队总业绩窗口。
	 *
	 * <p>未推广空白快照的G_day默认为0，可以参与Gsmooth均值，但不能单独触发G7区间。
	 * 因此这里只看历史快照中的昨日团队总业绩或今日团队总业绩是否大于0。</p>
	 *
	 * @param previousSnapshots 收益日前最近6个自然日内最多6条已计算快照
	 * @return true表示最近窗口内有团队总业绩
	 */
	private boolean hasG7TeamTotalPerformanceHistory(List<StakeHostingDailyTeamPerformance> previousSnapshots) {
		if (CollectionUtil.isEmpty(previousSnapshots)) {
			return false;
		}
		return previousSnapshots.stream()
			.anyMatch(item -> hasG7TeamTotalPerformance(item.getPreviousTeamTotalPerformance(), item.getCurrentTeamTotalPerformance()));
	}

	/**
	 * 从最近6个自然日历史快照中提取G_day用于Gsmooth均值。
	 *
	 * <p>这里不按收益率来源过滤；未推广规则当天的G_day默认为0，也参与最近最多7天均值。</p>
	 *
	 * @param previousSnapshots 收益日前最近6个自然日内最多6条已计算快照
	 * @return 历史G_day列表，单位%
	 */
	private List<BigDecimal> extractGDays(List<StakeHostingDailyTeamPerformance> previousSnapshots) {
		if (CollectionUtil.isEmpty(previousSnapshots)) {
			return java.util.Collections.emptyList();
		}
		return previousSnapshots.stream()
			.map(item -> nvl(item.getGDay()))
			.collect(Collectors.toList());
	}

	/**
	 * 使用当天G_day和前6个已计算G_day计算最近最多7天滚动平均。
	 *
	 * <p>Gsmooth用于平滑单日G值波动。当天G_day一定参与平均，再向前查询最多6条已经计算完成的历史G_day。
	 * 如果历史不足6天，就按实际可用天数平均，不强制补满7天。</p>
	 *
	 * @param previousGDays 收益日前最近最多6条历史G_day，按日期倒序
	 * @param currentGDay 当天单日增长率，单位%
	 * @return Gsmooth，单位%
	 */
	private BigDecimal calculateGSmooth(List<BigDecimal> previousGDays, BigDecimal currentGDay) {
		// 先把当天G_day放入汇总，确保当天最新增长变化会立即影响Gsmooth。
		BigDecimal total = nvl(currentGDay);
		int count = 1;
		// 历史G_day已经在外层批量预加载，避免每个用户计算Gsmooth时再单独查库。
		if (CollectionUtil.isNotEmpty(previousGDays)) {
			// 历史G_day逐条累加，空值兜底为0，并按实际条数作为平均分母。
			for (BigDecimal previousGDay : previousGDays) {
				total = total.add(nvl(previousGDay));
				count++;
			}
		}
		// 不足7天时按已有天数平均，保留4位小数供收益率区间配置匹配。
		return total.divide(new BigDecimal(count), 4, RoundingMode.HALF_UP);
	}

	/**
	 * 查询托管订单并做空值校验。
	 *
	 * @param orderId 托管订单ID
	 * @return 托管订单实体
	 */
	private StakeHostingOrder getOrder(Long orderId) {
		if (orderId == null) {
			throw new ServiceException("托管订单ID不能为空");
		}
		StakeHostingOrder order = stakeHostingOrderMapper.selectById(orderId);
		if (order == null) {
			throw new ServiceException("托管订单不存在");
		}
		return order;
	}

	/**
	 * 将可空金额兜底为0。
	 *
	 * @param value 可空金额
	 * @return 非空金额
	 */
	private BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	/**
	 * 从 yyyyMMddHHmmss 数字时间中截取统计日。
	 *
	 * @param time 数字时间
	 * @return 统计日，格式yyyyMMdd
	 */
	private Integer statDay(Long time) {
		return Integer.valueOf(String.valueOf(time).substring(0, 8));
	}

	/**
	 * 将 Date 转成统计日。
	 *
	 * @param time 日期时间
	 * @return 统计日，格式yyyyMMdd
	 */
	private Integer statDay(Date time) {
		return Integer.valueOf(DateUtil.format(time, "yyyyMMdd"));
	}

	/**
	 * 计算下一自然日。
	 *
	 * @param day 当前日期，格式yyyyMMdd
	 * @return 下一自然日，格式yyyyMMdd
	 */
	private Integer nextDay(Integer day) {
		return Integer.valueOf(LocalDate.parse(String.valueOf(day), DAY_FORMATTER).plusDays(1).format(DAY_FORMATTER));
	}

	/**
	 * 计算上一自然日。
	 *
	 * @param day 当前日期，格式yyyyMMdd
	 * @return 上一自然日，格式yyyyMMdd
	 */
	private Integer previousDay(Integer day) {
		return Integer.valueOf(LocalDate.parse(String.valueOf(day), DAY_FORMATTER).minusDays(1).format(DAY_FORMATTER));
	}

	/**
	 * 计算指定日期之前N天。
	 *
	 * @param day 当前日期，格式yyyyMMdd
	 * @param days 回退天数
	 * @return 回退后的日期，格式yyyyMMdd
	 */
	private Integer beforeDays(Integer day, int days) {
		return Integer.valueOf(LocalDate.parse(String.valueOf(day), DAY_FORMATTER).minusDays(days).format(DAY_FORMATTER));
	}
}
