package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 托管每周新增小区业绩Service业务层处理
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
		if (WEEKLY_STATUS_DONE == safeStatus(order.getWeeklyPerformanceStatus())) {
			return;
		}
		if (order.getPerformanceStartTime() == null || order.getPerformanceEndTime() == null) {
			markOrderFailed(orderId, "周新增业绩开始/结束时间为空");
			throw new ServiceException("周新增业绩开始/结束时间为空");
		}
		Long weekStartTime = weekStartTime(order.getPerformanceStartTime());
		Long weekEndTime = weekEndTime(order.getPerformanceStartTime());
		if (order.getPerformanceEndTime() <= weekEndTime) {
			markOrderDone(orderId, "本周内到期，不计入周新增业绩");
			return;
		}
		boolean locked = stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, orderId)
			.in(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_QUEUED, WEEKLY_STATUS_PROCESSING)
			.set(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_PROCESSING)
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
		if (!locked) {
			StakeHostingOrder latest = stakeHostingOrderService.getById(orderId);
			if (latest != null && WEEKLY_STATUS_DONE == safeStatus(latest.getWeeklyPerformanceStatus())) {
				return;
			}
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
		BigDecimal amount = order.getStakeUsdtAmount() == null ? BigDecimal.ZERO : order.getStakeUsdtAmount();
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			markOrderDone(order.getId(), "订单金额小于等于0，不计入周新增业绩");
			return false;
		}
		UserInfo buyer = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, order.getUserId())
			.one();
		if (buyer == null) {
			throw new ServiceException("周新增业绩用户不存在");
		}

		upsertPerformance(buyer.getUserId(), buyer.getAccount(), weekStartTime, weekEndTime, amount, BigDecimal.ZERO);
		List<UserRelation> parents = userRelationService.lambdaQuery()
			.eq(UserRelation::getPosUserId, buyer.getUserId())
			.eq(UserRelation::getActiveFlag, 1)
			.gt(UserRelation::getDistance, 0)
			.orderByAsc(UserRelation::getDistance)
			.list();
		LinkedHashSet<Long> affectedUserIds = new LinkedHashSet<>();
		affectedUserIds.add(buyer.getUserId());
		if (CollectionUtil.isNotEmpty(parents)) {
			List<Long> parentIds = parents.stream()
				.map(UserRelation::getParUserId)
				.collect(Collectors.toList());
			List<UserInfo> parentUsers = userInfoService.lambdaQuery()
				.in(UserInfo::getUserId, parentIds)
				.list();
			Map<Long, UserInfo> parentMap = parentUsers.stream()
				.collect(Collectors.toMap(UserInfo::getUserId, Function.identity(), (a, b) -> a));
			for (Long parentId : parentIds) {
				UserInfo parent = parentMap.get(parentId);
				if (parent == null) {
					continue;
				}
				upsertPerformance(parent.getUserId(), parent.getAccount(), weekStartTime, weekEndTime, BigDecimal.ZERO, amount);
				affectedUserIds.add(parent.getUserId());
			}
		}
		for (Long userId : affectedUserIds) {
			recalculateCommunityPerformance(userId, weekStartTime);
		}
		return true;
	}

	private void upsertPerformance(Long userId, String account, Long weekStartTime, Long weekEndTime,
								   BigDecimal selfAmount, BigDecimal teamAmount) {
		baseMapper.upsertWeeklyPerformance(userId, account, weekStartTime, weekEndTime,
			selfAmount == null ? BigDecimal.ZERO : selfAmount,
			teamAmount == null ? BigDecimal.ZERO : teamAmount);
	}

	private void recalculateCommunityPerformance(Long userId, Long weekStartTime) {
		List<UserInfo> directUsers = userInfoService.lambdaQuery()
			.eq(UserInfo::getInviteUserId, userId)
			.eq(UserInfo::getDeleted, 0)
			.list();
		if (CollectionUtil.isEmpty(directUsers)) {
			updateCommunity(userId, weekStartTime, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
			return;
		}
		List<Long> directUserIds = directUsers.stream().map(UserInfo::getUserId).collect(Collectors.toList());
		List<StakeHostingWeeklyCommunityPerformance> performanceList = baseMapper.selectByUserIdsAndWeek(directUserIds, weekStartTime);
		Map<Long, StakeHostingWeeklyCommunityPerformance> performanceMap = CollectionUtil.isEmpty(performanceList)
			? Collections.emptyMap()
			: performanceList.stream().collect(Collectors.toMap(StakeHostingWeeklyCommunityPerformance::getUserId, Function.identity(), (a, b) -> a));
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal max = BigDecimal.ZERO;
		int effectiveLineCount = 0;
		for (Long directUserId : directUserIds) {
			StakeHostingWeeklyCommunityPerformance performance = performanceMap.get(directUserId);
			BigDecimal linePerformance = BigDecimal.ZERO;
			if (performance != null) {
				linePerformance = nvl(performance.getSelfNewPerformance()).add(nvl(performance.getTeamNewPerformance()));
			}
			if (linePerformance.compareTo(BigDecimal.ZERO) > 0) {
				effectiveLineCount++;
			}
			total = total.add(linePerformance);
			if (linePerformance.compareTo(max) > 0) {
				max = linePerformance;
			}
		}
		BigDecimal community = effectiveLineCount <= 1 ? BigDecimal.ZERO : total.subtract(max);
		if (community.compareTo(BigDecimal.ZERO) < 0) {
			community = BigDecimal.ZERO;
		}
		updateCommunity(userId, weekStartTime, total, max, community);
	}

	private void updateCommunity(Long userId, Long weekStartTime, BigDecimal total, BigDecimal max, BigDecimal community) {
		lambdaUpdate()
			.eq(StakeHostingWeeklyCommunityPerformance::getUserId, userId)
			.eq(StakeHostingWeeklyCommunityPerformance::getWeekStartTime, weekStartTime)
			.set(StakeHostingWeeklyCommunityPerformance::getTotalLinePerformance, total)
			.set(StakeHostingWeeklyCommunityPerformance::getMaxLinePerformance, max)
			.set(StakeHostingWeeklyCommunityPerformance::getCommunityNewPerformance, community)
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

	private int safeStatus(Integer status) {
		return status == null ? 0 : status;
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
}
