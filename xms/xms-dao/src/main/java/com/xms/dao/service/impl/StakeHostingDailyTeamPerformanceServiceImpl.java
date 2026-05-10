package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.StakeHostingDailyTeamPerformance;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserRelation;
import com.xms.dao.mapper.StakeHostingDailyTeamPerformanceMapper;
import com.xms.dao.mapper.StakeHostingOrderMapper;
import com.xms.dao.service.IStakeHostingDailyTeamPerformanceService;
import com.xms.dao.service.IStakeHostingStaticRateConfigService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserRelationService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 托管G7每日团队TVL与收益率快照Service业务层处理
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
	private static final BigDecimal LOW_BASE = new BigDecimal("100");
	private static final BigDecimal MAX_G_DAY = new BigDecimal("200");
	private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	private static final BigDecimal HUNDRED = new BigDecimal("100");

	private final StakeHostingOrderMapper stakeHostingOrderMapper;
	private final UserInfoService userInfoService;
	private final UserRelationService userRelationService;
	private final IStakeHostingStaticRateConfigService staticRateConfigService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void recordOrderTeamNewAmount(Long orderId) {
		StakeHostingOrder order = getOrder(orderId);
		if (order.getPerformanceStartTime() == null) {
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
		recordParentAmount(order.getUserId(), statDay(order.getPerformanceStartTime()), order.getStakeUsdtAmount(), true);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
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

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void prepareDailySnapshots(Integer rewardDay, List<Long> rewardUserIds) {
		if (rewardDay == null) {
			throw new ServiceException("G7收益率快照日期不能为空");
		}
		Set<Long> userIds = new LinkedHashSet<>();
		List<Long> statUserIds = baseMapper.selectUserIdsByStatDay(rewardDay);
		if (CollectionUtil.isNotEmpty(statUserIds)) {
			userIds.addAll(statUserIds);
		}
		if (CollectionUtil.isNotEmpty(rewardUserIds)) {
			userIds.addAll(rewardUserIds);
		}
		for (Long userId : userIds) {
			prepareOneSnapshot(userId, rewardDay);
		}
	}

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

	@Override
	public boolean hasTeamTvl(Long userId, Integer rewardDay) {
		StakeHostingDailyTeamPerformance snapshot = getCalculatedSnapshot(userId, rewardDay);
		return snapshot != null && snapshot.getCurrentTeamTvl() != null && snapshot.getCurrentTeamTvl().compareTo(BigDecimal.ZERO) > 0;
	}

	/**
	 * 给买家的所有有效上级记录G7团队新增或到期USDT金额。
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
	 * 生成单个用户某天的G7团队TVL和基础收益率快照。
	 *
	 * @param userId 用户ID
	 * @param rewardDay 收益日，格式yyyyMMdd
	 */
	private void prepareOneSnapshot(Long userId, Integer rewardDay) {
		UserInfo user = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (user == null) {
			return;
		}
		baseMapper.upsertEmptyDay(user.getUserId(), user.getAccount(), rewardDay);
		StakeHostingDailyTeamPerformance snapshot = lambdaQuery()
			.eq(StakeHostingDailyTeamPerformance::getUserId, userId)
			.eq(StakeHostingDailyTeamPerformance::getStatDay, rewardDay)
			.eq(StakeHostingDailyTeamPerformance::getDeleted, 0)
			.one();
		if (snapshot == null || (snapshot.getCalcStatus() != null && snapshot.getCalcStatus() == CALC_STATUS_DONE)) {
			return;
		}
		StakeHostingDailyTeamPerformance latest = baseMapper.selectLatestBefore(userId, rewardDay);
		Integer previousDay = previousDay(rewardDay);
		BigDecimal previousTvl = latest == null || latest.getStatDay() == null || latest.getStatDay() < previousDay
			? nvl(baseMapper.selectTeamTvlAt(userId, previousDayEndTime(rewardDay))) : nvl(latest.getCurrentTeamTvl());
		BigDecimal currentTvl = previousTvl.add(nvl(snapshot.getTeamNewAmount())).subtract(nvl(snapshot.getTeamExpiredAmount()))
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if (currentTvl.compareTo(BigDecimal.ZERO) < 0) {
			currentTvl = BigDecimal.ZERO.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		}
		BigDecimal gDay = calculateGDay(previousTvl, currentTvl);
		BigDecimal gSmooth = calculateGSmooth(userId, rewardDay, gDay);
		BigDecimal staticRate = staticRateConfigService.matchStaticRate(gSmooth);
		lambdaUpdate()
			.eq(StakeHostingDailyTeamPerformance::getId, snapshot.getId())
			.set(StakeHostingDailyTeamPerformance::getPreviousTeamTvl, previousTvl)
			.set(StakeHostingDailyTeamPerformance::getCurrentTeamTvl, currentTvl)
			.set(StakeHostingDailyTeamPerformance::getGDay, gDay)
			.set(StakeHostingDailyTeamPerformance::getGSmooth, gSmooth)
			.set(StakeHostingDailyTeamPerformance::getBaseStaticRate, staticRate)
			.set(StakeHostingDailyTeamPerformance::getRateSource, RATE_SOURCE_G7)
			.set(StakeHostingDailyTeamPerformance::getCalcStatus, CALC_STATUS_DONE)
			.set(StakeHostingDailyTeamPerformance::getUpdateTime, new Date())
			.update();
	}

	/**
	 * 按G7规则计算单日团队TVL增长率。
	 *
	 * @param previousTvl 昨日伞下团队有效托管USDT TVL
	 * @param currentTvl 当日伞下团队有效托管USDT TVL
	 * @return 单日增长率，单位%
	 */
	private BigDecimal calculateGDay(BigDecimal previousTvl, BigDecimal currentTvl) {
		previousTvl = nvl(previousTvl);
		currentTvl = nvl(currentTvl);
		if (previousTvl.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
		}
		BigDecimal denominator = previousTvl.max(LOW_BASE);
		BigDecimal gDay = currentTvl.subtract(previousTvl).multiply(HUNDRED)
			.divide(denominator, 4, RoundingMode.HALF_UP);
		return gDay.compareTo(MAX_G_DAY) > 0 ? MAX_G_DAY.setScale(4, RoundingMode.HALF_UP) : gDay;
	}

	/**
	 * 使用当天G_day和前6个已计算G_day计算最近最多7天滚动平均。
	 *
	 * @param userId 用户ID
	 * @param rewardDay 收益日，格式yyyyMMdd
	 * @param currentGDay 当天单日增长率，单位%
	 * @return Gsmooth，单位%
	 */
	private BigDecimal calculateGSmooth(Long userId, Integer rewardDay, BigDecimal currentGDay) {
		BigDecimal total = nvl(currentGDay);
		int count = 1;
		List<BigDecimal> previousGDays = baseMapper.selectRecentGDayBefore(userId, rewardDay);
		if (CollectionUtil.isNotEmpty(previousGDays)) {
			for (BigDecimal previousGDay : previousGDays) {
				total = total.add(nvl(previousGDay));
				count++;
			}
		}
		return total.divide(new BigDecimal(count), 4, RoundingMode.HALF_UP);
	}

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

	private BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private Integer statDay(Long time) {
		return Integer.valueOf(String.valueOf(time).substring(0, 8));
	}

	private Integer nextDay(Integer day) {
		return Integer.valueOf(LocalDate.parse(String.valueOf(day), DAY_FORMATTER).plusDays(1).format(DAY_FORMATTER));
	}

	private Integer previousDay(Integer day) {
		return Integer.valueOf(LocalDate.parse(String.valueOf(day), DAY_FORMATTER).minusDays(1).format(DAY_FORMATTER));
	}

	private Long previousDayEndTime(Integer day) {
		return Long.valueOf(LocalDate.parse(String.valueOf(day), DAY_FORMATTER).minusDays(1).format(DAY_FORMATTER) + "235959");
	}
}
