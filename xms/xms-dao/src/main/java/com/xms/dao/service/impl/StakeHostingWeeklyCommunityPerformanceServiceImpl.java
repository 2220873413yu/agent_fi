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
		processOrderWeeklyPerformance(orderId, false);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void processOrderWeeklyExpirePerformance(Long orderId) {
		processOrderWeeklyPerformance(orderId, true);
	}

	/**
	 * 按订单事件重算周新增小区业绩。
	 *
	 * <p>订单生效和订单到期都会影响上级各直推区的有效积分快照，但两类事件分别维护不同状态字段：
	 * 生效事件使用 `weekly_performance_*`，到期事件使用 `weekly_expire_performance_*`，方便排查新增和到期是否各自处理。</p>
	 *
	 * @param orderId 托管订单ID
	 * @param expireEvent true表示订单到期完成后的重算；false表示订单生效后的新增重算
	 */
	private void processOrderWeeklyPerformance(Long orderId, boolean expireEvent) {
		if (orderId == null) {
			return;
		}
		StakeHostingOrder order = stakeHostingOrderService.getById(orderId);
		if (order == null) {
			log.info("托管周小区业绩重算跳过，订单不存在 orderId={}, expireEvent={}", orderId, expireEvent);
			return;
		}
		if (order.getPerformanceStartTime() == null || order.getPerformanceEndTime() == null) {
			markOrderFailed(orderId, expireEvent, "周业绩开始/结束时间为空");
			throw new ServiceException("周业绩开始/结束时间为空");
		}
		if (expireEvent && !isFinishedOrder(order)) {
			markOrderFailed(orderId, true, "订单未完成，不能处理到期周业绩");
			throw new ServiceException("订单未完成，不能处理到期周业绩");
		}

		Long eventTime = getPerformanceEventTime(order, expireEvent);
		Long weekStartTime = weekStartTime(eventTime);
		Long weekEndTime = weekEndTime(eventTime);
		if (!expireEvent && order.getPerformanceEndTime() <= weekEndTime) {
			markOrderDone(orderId, false, "本周内到期，不计入周新增业绩");
			return;
		}

		boolean locked = lockOrderWeeklyStatus(orderId, expireEvent);
		if (!locked) {
			log.info("托管周小区业绩重算跳过，订单状态不允许处理 orderId={}, expireEvent={}", orderId, expireEvent);
			return;
		}

		try {
			if (doProcessOrder(order, weekStartTime, weekEndTime, expireEvent)) {
				markOrderDone(orderId, expireEvent, null);
			}
		} catch (Exception e) {
			markOrderFailed(orderId, expireEvent, e.getMessage());
			throw e;
		}
	}

	/**
	 * 重算订单影响到的用户周新增小区业绩。
	 *
	 * <p>本方法不直接把订单积分加到某个上级身上，而是以订单买家和所有上级为受影响用户，
	 * 分别重新计算他们在本周窗口内的个人/团队净新增积分，以及上周末、本周末两次直推区有效积分快照。
	 * 全球分红最终使用的 `community_new_performance` 就来自这些快照差额。</p>
	 *
	 * @param order 触发周业绩重算的托管订单
	 * @param weekStartTime 事件所属自然周开始时间，格式yyyyMMddHHmmss
	 * @param weekEndTime 事件所属自然周结束时间，格式yyyyMMddHHmmss
	 * @param expireEvent true表示订单到期完成触发的重算；false表示订单生效触发的新增重算
	 * @return true表示已完成受影响用户重算；false表示订单积分为0等原因已跳过
	 */
	private boolean doProcessOrder(StakeHostingOrder order, Long weekStartTime, Long weekEndTime, boolean expireEvent) {
		// 订单积分来自下单时的套餐权重快照；1天套餐权重为0时不参与全球分红周业绩。
		BigDecimal points = getOrderPerformancePoints(order);
		if (points.compareTo(BigDecimal.ZERO) <= 0) {
			markOrderDone(order.getId(), expireEvent, "订单业绩积分小于等于0，不计入周新增业绩");
			return false;
		}
		// 先定位订单买家，后续以买家为起点找所有上级；买家自己也需要刷新周业绩记录和小区快照。
		UserInfo buyer = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, order.getUserId())
			.one();
		if (buyer == null) {
			throw new ServiceException("周新增业绩用户不存在");
		}

		// 受影响用户 = 买家自己 + 买家的所有上级。买家自己下单不直接形成自己的小区业绩，但其快照仍需重算。
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
			// 逐个重算受影响用户的周业绩；用户不存在时跳过，避免历史关系脏数据中断整笔订单处理。
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getUserId, userId)
				.one();
			if (userInfo == null) {
				continue;
			}
			// 个人/团队净新增积分按本周新增订单积分减本周到期订单积分汇总，使用订单 performance_points 快照。
			BigDecimal selfNetPoints = nvl(baseMapper.selectSelfWeeklyNetPoints(userId, weekStartTime, weekEndTime));
			BigDecimal teamNetPoints = nvl(baseMapper.selectTeamWeeklyNetPoints(userId, weekStartTime, weekEndTime));
			// 先落本周基础汇总，再基于当前订单有效状态重算直推区快照和 community_new_performance。
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

	/**
	 * 重算用户本周新增小区业绩快照。
	 *
	 * <p>全球分红不直接使用个人/团队净新增积分，而是使用小区有效积分的周差额。
	 * 本方法按用户的每个直推用户划分直推区，分别计算上周末和本周末的小区有效积分，
	 * 最后用 `本周末小区有效积分 - 上周末小区有效积分` 得到 `community_new_performance`。</p>
	 *
	 * @param userId 需要重算周小区业绩的用户ID
	 * @param weekStartTime 自然周开始时间，格式yyyyMMddHHmmss
	 * @param weekEndTime 自然周结束时间，格式yyyyMMddHHmmss
	 */
	private void recalculateCommunityPerformance(Long userId, Long weekStartTime, Long weekEndTime) {
		// 每个直接推荐用户代表当前用户的一条直推区；没有直推区时，小区业绩固定为0。
		List<UserInfo> directUsers = userInfoService.lambdaQuery()
			.eq(UserInfo::getInviteUserId, userId)
			.eq(UserInfo::getDeleted, 0)
			.list();
		if (CollectionUtil.isEmpty(directUsers)) {
			// 无直推用户无法形成小区，清空本周直推区快照和周新增小区业绩。
			updateCommunity(userId, weekStartTime, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
			return;
		}
		// 上周末快照使用本周开始前一秒，代表本周开始前已经存在的有效小区积分。
		LineSnapshot previous = calculateLineSnapshot(directUsers, previousSecond(weekStartTime));
		// 本周末快照使用周日23:59:59，代表本周结算时仍有效的直推区积分。
		LineSnapshot current = calculateLineSnapshot(directUsers, weekEndTime);
		// 周新增小区业绩允许为负数；负数只做记录，全球分红发放时不会参与分母。
		BigDecimal communityDelta = current.community.subtract(previous.community)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 保存直推区总积分、最大区积分、上周/本周小区快照和最终周新增小区业绩。
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

	/**
	 * 将订单对应的周业绩事件标记为处理中。
	 *
	 * @param orderId 托管订单ID
	 * @param expireEvent true表示锁定到期重算状态，false表示锁定新增重算状态
	 * @return 是否成功锁定
	 */
	private boolean lockOrderWeeklyStatus(Long orderId, boolean expireEvent) {
		if (expireEvent) {
			return stakeHostingOrderService.lambdaUpdate()
				.eq(StakeHostingOrder::getId, orderId)
				.and(wrapper -> wrapper.isNull(StakeHostingOrder::getWeeklyExpirePerformanceStatus)
					.or()
					.ne(StakeHostingOrder::getWeeklyExpirePerformanceStatus, WEEKLY_STATUS_DONE))
				.set(StakeHostingOrder::getWeeklyExpirePerformanceStatus, WEEKLY_STATUS_PROCESSING)
				.set(StakeHostingOrder::getUpdateTime, new Date())
				.update();
		}
		return stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, orderId)
			.and(wrapper -> wrapper.isNull(StakeHostingOrder::getWeeklyPerformanceStatus)
				.or()
				.ne(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_DONE))
			.set(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_PROCESSING)
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
	}

	/**
	 * 标记订单对应周业绩事件处理完成。
	 *
	 * @param orderId 托管订单ID
	 * @param expireEvent true表示到期重算状态，false表示新增重算状态
	 * @param skipReason 跳过原因；正常处理完成时为空
	 */
	private void markOrderDone(Long orderId, boolean expireEvent, String skipReason) {
		if (expireEvent) {
			stakeHostingOrderService.lambdaUpdate()
				.eq(StakeHostingOrder::getId, orderId)
				.set(StakeHostingOrder::getWeeklyExpirePerformanceStatus, WEEKLY_STATUS_DONE)
				.set(StakeHostingOrder::getWeeklyExpirePerformanceSkipReason, skipReason)
				.set(StakeHostingOrder::getWeeklyExpirePerformanceTime, new Date())
				.set(StakeHostingOrder::getUpdateTime, new Date())
				.update();
			return;
		}
		stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, orderId)
			.set(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_DONE)
			.set(StakeHostingOrder::getWeeklyPerformanceSkipReason, skipReason)
			.set(StakeHostingOrder::getWeeklyPerformanceTime, new Date())
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
	}

	/**
	 * 标记订单对应周业绩事件处理失败。
	 *
	 * @param orderId 托管订单ID
	 * @param expireEvent true表示到期重算状态，false表示新增重算状态
	 * @param reason 失败原因
	 */
	private void markOrderFailed(Long orderId, boolean expireEvent, String reason) {
		if (expireEvent) {
			stakeHostingOrderService.lambdaUpdate()
				.eq(StakeHostingOrder::getId, orderId)
				.set(StakeHostingOrder::getWeeklyExpirePerformanceStatus, WEEKLY_STATUS_FAILED)
				.set(StakeHostingOrder::getWeeklyExpirePerformanceSkipReason, reason)
				.set(StakeHostingOrder::getUpdateTime, new Date())
				.update();
			return;
		}
		stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, orderId)
			.set(StakeHostingOrder::getWeeklyPerformanceStatus, WEEKLY_STATUS_FAILED)
			.set(StakeHostingOrder::getWeeklyPerformanceSkipReason, reason)
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
	}

	private Long getPerformanceEventTime(StakeHostingOrder order, boolean expireEvent) {
		if (expireEvent && order.getFinishTime() != null) {
			return formatDate(order.getFinishTime());
		}
		return order.getPerformanceStartTime();
	}

	private boolean isFinishedOrder(StakeHostingOrder order) {
		return order.getStatus() != null && order.getStatus() == StakeHostingOrderServiceImpl.STATUS_FINISHED;
	}

	/**
	 * 读取订单参与周新增小区业绩的积分。
	 *
	 * <p>优先使用订单下单时保存的 `performance_points` 快照；如果快照缺失，只允许使用订单上的
	 * `performance_coefficient` 快照重新计算，不能默认按1倍兜底，避免1天套餐被错误计入全球分红权重。</p>
	 *
	 * @param order 托管订单
	 * @return 订单业绩积分，单位为积分
	 */
	private BigDecimal getOrderPerformancePoints(StakeHostingOrder order) {
		if (order.getPerformancePoints() != null) {
			return order.getPerformancePoints();
		}
		if (order.getPerformanceCoefficient() == null) {
			throw new ServiceException("订单业绩积分系数快照为空");
		}
		BigDecimal coefficient = order.getPerformanceCoefficient();
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
