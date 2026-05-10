package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.domain.StakeHostingWeeklyCommunityPerformance;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserRelation;
import com.xms.dao.mapper.StakeHostingWeeklyCommunityPerformanceMapper;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IStakeHostingWeeklyCommunityPerformanceService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserRelationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 托管每周新增小区业绩Service业务层处理.
 *
 * <p>当前口径按“直推区有效积分快照”计算：本周新增小区业绩 =
 * 本周末小区有效积分快照 - 上周末小区有效积分快照，允许为负数。</p>
 *
 * @author xms
 */
@Slf4j
@Service
@AllArgsConstructor
public class StakeHostingWeeklyCommunityPerformanceServiceImpl
	extends XmsDataServiceImpl<StakeHostingWeeklyCommunityPerformanceMapper, StakeHostingWeeklyCommunityPerformance>
	implements IStakeHostingWeeklyCommunityPerformanceService {

	public static final int WEEKLY_STATUS_QUEUED = 1;
	public static final int WEEKLY_STATUS_PROCESSING = 2;
	public static final int WEEKLY_STATUS_DONE = 3;
	public static final int WEEKLY_STATUS_FAILED = 4;
	private static final ZoneId BIZ_ZONE = ZoneId.of("Asia/Shanghai");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private final IStakeHostingOrderService stakeHostingOrderService;
	private final UserInfoService userInfoService;
	private final UserRelationService userRelationService;

	@Override
	public List<StakeHostingWeeklyCommunityPerformance> selectStakeHostingWeeklyCommunityPerformanceList(StakeHostingWeeklyCommunityPerformance performance) {
		return baseMapper.selectStakeHostingWeeklyCommunityPerformanceList(performance);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void processOrderWeeklyPerformance(Long orderId) {
		if (orderId == null) {
			return;
		}
		StakeHostingOrder order = stakeHostingOrderService.getById(orderId);
		if (order == null) {
			log.info("托管周新增业绩跳过，订单不存在 orderId:{}", orderId);
			return;
		}
		if (order.getPerformanceStartTime() == null || order.getPerformanceEndTime() == null) {
			markOrderFailed(orderId, "周新增业绩开始/结束时间为空");
			throw new ServiceException("周新增业绩开始/结束时间为空");
		}

		Long eventTime = getPerformanceEventTime(order);
		Long weekStartTime = weekStartTime(eventTime);
		Long weekEndTime = weekEndTime(eventTime);
		if (!isFinishedOrder(order) && order.getPerformanceEndTime() <= weekEndTime) {
			markOrderDone(orderId, "本周内到期，不计入周新增业绩");
			return;
		}

		boolean locked = stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, orderId)
			.set(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_PROCESSING)
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
		if (!locked) {
			log.info("托管周新增业绩跳过，订单状态不允许处理 orderId:{}", orderId);
			return;
		}

		try {
			if (doProcessOrder(order, weekStartTime, weekEndTime)) {
				markOrderDone(orderId, null);
			}
		} catch (Exception e) {
			markOrderFailed(orderId, e.getMessage());
			throw e;
		}
	}

	private boolean doProcessOrder(StakeHostingOrder order, Long weekStartTime, Long weekEndTime) {
		BigDecimal points = getOrderPerformancePoints(order);
		if (points.compareTo(BigDecimal.ZERO) <= 0) {
			markOrderDone(order.getId(), "订单业绩积分小于等于0，不计入周新增业绩");
			return false;
		}
		UserInfo buyer = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, order.getUserId())
			.one();
		if (buyer == null) {
			throw new ServiceException("周新增业绩用户不存在");
		}

		LinkedHashSet<Long> affectedUserIds = new LinkedHashSet<>();
		affectedUserIds.add(buyer.getUserId());
		List<UserRelation> parents = userRelationService.lambdaQuery()
			.eq(UserRelation::getPosUserId, buyer.getUserId())
			.eq(UserRelation::getActiveFlag, 1)
			.gt(UserRelation::getDistance, 0)
			.orderByAsc(UserRelation::getDistance)
			.list();
		if (CollectionUtil.isNotEmpty(parents)) {
			affectedUserIds.addAll(parents.stream()
				.map(UserRelation::getParUserId)
				.collect(Collectors.toList()));
		}

		for (Long userId : affectedUserIds) {
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getUserId, userId)
				.one();
			if (userInfo == null) {
				continue;
			}
			BigDecimal selfNetPoints = nvl(baseMapper.selectSelfWeeklyNetPoints(userId, weekStartTime, weekEndTime));
			BigDecimal teamNetPoints = nvl(baseMapper.selectTeamWeeklyNetPoints(userId, weekStartTime, weekEndTime));
			upsertPerformance(userInfo.getUserId(), userInfo.getAccount(), weekStartTime, weekEndTime, selfNetPoints, teamNetPoints);
			recalculateCommunityPerformance(userId, weekStartTime, weekEndTime);
		}
		return true;
	}

	private void upsertPerformance(Long userId, String account, Long weekStartTime, Long weekEndTime,
								   BigDecimal selfAmount, BigDecimal teamAmount) {
		baseMapper.upsertWeeklyPerformance(userId, account, weekStartTime, weekEndTime,
			nvl(selfAmount), nvl(teamAmount));
	}

	private void recalculateCommunityPerformance(Long userId, Long weekStartTime, Long weekEndTime) {
		List<UserInfo> directUsers = userInfoService.lambdaQuery()
			.eq(UserInfo::getInviteUserId, userId)
			.eq(UserInfo::getDeleted, 0)
			.list();
		if (CollectionUtil.isEmpty(directUsers)) {
			updateCommunity(userId, weekStartTime, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
			return;
		}
		LineSnapshot previous = calculateLineSnapshot(directUsers, previousSecond(weekStartTime));
		LineSnapshot current = calculateLineSnapshot(directUsers, weekEndTime);
		BigDecimal communityDelta = current.community.subtract(previous.community)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		updateCommunity(userId, weekStartTime, current.total, current.max, previous.community, current.community, communityDelta);
	}

	private LineSnapshot calculateLineSnapshot(List<UserInfo> directUsers, Long snapshotTime) {
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal max = BigDecimal.ZERO;
		int effectiveLineCount = 0;
		for (UserInfo directUser : directUsers) {
			BigDecimal linePoints = nvl(baseMapper.selectLineValidPoints(directUser.getUserId(), snapshotTime));
			if (linePoints.compareTo(BigDecimal.ZERO) > 0) {
				effectiveLineCount++;
			}
			total = total.add(linePoints);
			if (linePoints.compareTo(max) > 0) {
				max = linePoints;
			}
		}
		BigDecimal community = effectiveLineCount <= 1 ? BigDecimal.ZERO : total.subtract(max);
		return new LineSnapshot(total, max, community);
	}

	private void updateCommunity(Long userId, Long weekStartTime, BigDecimal total, BigDecimal max,
								 BigDecimal previousCommunity, BigDecimal currentCommunity, BigDecimal communityDelta) {
		lambdaUpdate()
			.eq(StakeHostingWeeklyCommunityPerformance::getUserId, userId)
			.eq(StakeHostingWeeklyCommunityPerformance::getWeekStartTime, weekStartTime)
			.set(StakeHostingWeeklyCommunityPerformance::getTotalLinePerformance, total)
			.set(StakeHostingWeeklyCommunityPerformance::getMaxLinePerformance, max)
			.set(StakeHostingWeeklyCommunityPerformance::getPreviousCommunityPerformance, previousCommunity)
			.set(StakeHostingWeeklyCommunityPerformance::getCurrentCommunityPerformance, currentCommunity)
			.set(StakeHostingWeeklyCommunityPerformance::getCommunityNewPerformance, communityDelta)
			.set(StakeHostingWeeklyCommunityPerformance::getUpdateTime, new Date())
			.update();
	}

	private void markOrderDone(Long orderId, String skipReason) {
		stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, orderId)
			.set(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_DONE)
			.set(StakeHostingOrder::getWeeklyPerformanceSkipReason, skipReason)
			.set(StakeHostingOrder::getWeeklyPerformanceTime, new Date())
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
	}

	private void markOrderFailed(Long orderId, String reason) {
		stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, orderId)
			.set(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_FAILED)
			.set(StakeHostingOrder::getWeeklyPerformanceSkipReason, reason)
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
	}

	private Long getPerformanceEventTime(StakeHostingOrder order) {
		if (isFinishedOrder(order) && order.getFinishTime() != null) {
			return formatDate(order.getFinishTime());
		}
		return order.getPerformanceStartTime();
	}

	private boolean isFinishedOrder(StakeHostingOrder order) {
		return order.getStatus() != null && order.getStatus() == StakeHostingOrderServiceImpl.STATUS_FINISHED;
	}

	private BigDecimal getOrderPerformancePoints(StakeHostingOrder order) {
		if (order.getPerformancePoints() != null) {
			return order.getPerformancePoints();
		}
		BigDecimal coefficient = order.getPerformanceCoefficient() == null ? BigDecimal.ONE : order.getPerformanceCoefficient();
		return nvl(order.getStakeUsdtAmount()).multiply(coefficient)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	private BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private Long weekStartTime(Long time) {
		return weekStartTimeOf(time);
	}

	public static Long weekStartTimeOf(Long time) {
		LocalDate date = parseTime(time).toLocalDate();
		LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		return formatTime(monday.atStartOfDay());
	}

	private Long weekEndTime(Long time) {
		return weekEndTimeOf(time);
	}

	public static Long weekEndTimeOf(Long time) {
		LocalDate date = parseTime(time).toLocalDate();
		LocalDate sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		return formatTime(LocalDateTime.of(sunday, LocalTime.of(23, 59, 59)));
	}

	private static Long previousSecond(Long time) {
		return formatTime(parseTime(time).minusSeconds(1));
	}

	private static LocalDateTime parseTime(Long time) {
		return LocalDateTime.parse(String.valueOf(time), TIME_FORMATTER);
	}

	private static Long formatTime(LocalDateTime time) {
		return Long.valueOf(time.format(TIME_FORMATTER));
	}

	public static Long formatDate(Date date) {
		return Long.valueOf(LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), BIZ_ZONE).format(TIME_FORMATTER));
	}

	public static Long plusDays(Long startTime, int days) {
		return Long.valueOf(LocalDateTime.parse(String.valueOf(startTime), TIME_FORMATTER).plusDays(days).format(TIME_FORMATTER));
	}

	private static class LineSnapshot {
		private final BigDecimal total;
		private final BigDecimal max;
		private final BigDecimal community;

		private LineSnapshot(BigDecimal total, BigDecimal max, BigDecimal community) {
			this.total = total;
			this.max = max;
			this.community = community;
		}
	}
}
