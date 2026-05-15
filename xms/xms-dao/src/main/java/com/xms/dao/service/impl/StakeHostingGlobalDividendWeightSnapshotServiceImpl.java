package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.mapper.StakeHostingGlobalDividendWeightSnapshotMapper;
import com.xms.dao.service.IStakeHostingGlobalDividendWeightSnapshotService;
import com.xms.dao.service.UserInfoService;
import lombok.AllArgsConstructor;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 托管全球分红权重快照Service业务层处理
 *
 * <p>全球分红不再在订单生效或到期时维护周新增字段，而是在每周结算任务中按结算时刻
 * 统一重算直推区合计权重、最大区权重、小区权重和本期分红权重。</p>
 *
 * @author xms
 */
@Service
@AllArgsConstructor
public class StakeHostingGlobalDividendWeightSnapshotServiceImpl
	extends XmsDataServiceImpl<StakeHostingGlobalDividendWeightSnapshotMapper, StakeHostingGlobalDividendWeightSnapshot>
	implements IStakeHostingGlobalDividendWeightSnapshotService {

	private static final ZoneId BIZ_ZONE = ZoneId.of("Asia/Shanghai");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	private final UserInfoService userInfoService;

	@Override
	public List<StakeHostingGlobalDividendWeightSnapshot> selectStakeHostingGlobalDividendWeightSnapshotList(StakeHostingGlobalDividendWeightSnapshot snapshot) {
		return baseMapper.selectStakeHostingGlobalDividendWeightSnapshotList(snapshot);
	}

	/**
	 * Recalculates and persists one week's global dividend weight snapshots.
	 *
	 * <p>The snapshot is based on effective hosting orders at {@code weekEndTime}. Each user's direct lines are
	 * recalculated from current order validity; expired orders naturally disappear from the current weight. The
	 * persisted {@code dividendWeight} is never negative and is the only weight used by the weekly global dividend
	 * task.</p>
	 *
	 * @param weekStartTime week start time in yyyyMMddHHmmss format
	 * @param weekEndTime week end time in yyyyMMddHHmmss format
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void prepareWeeklySnapshots(Long weekStartTime, Long weekEndTime) {
		List<UserInfo> users = userInfoService.lambdaQuery()
			.eq(UserInfo::getDeleted, 0)
			.list();
		if (CollectionUtil.isEmpty(users)) {
			return;
		}
		Map<Long, List<UserInfo>> directUserMap = users.stream()
			.filter(user -> user.getInviteUserId() != null)
			.collect(Collectors.groupingBy(UserInfo::getInviteUserId));
		for (UserInfo user : users) {
			LineWeight lineWeight = calculateLineWeight(directUserMap.get(user.getUserId()), weekEndTime);
			BigDecimal previousCommunityWeight = nvl(baseMapper.selectPreviousCommunityWeight(user.getUserId(), weekStartTime));
			BigDecimal dividendWeight = lineWeight.communityWeight.subtract(previousCommunityWeight)
				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			if (dividendWeight.compareTo(BigDecimal.ZERO) < 0) {
				dividendWeight = BigDecimal.ZERO;
			}
			baseMapper.upsertWeightSnapshot(user.getUserId(), user.getAccount(), weekStartTime, weekEndTime,
				lineWeight.totalLineWeight, lineWeight.maxLineWeight, lineWeight.communityWeight,
				previousCommunityWeight, dividendWeight);
		}
	}

	/**
	 * Calculates one user's direct-line weights at the global dividend settlement time.
	 *
	 * @param directUsers direct invitees; each direct user represents one direct line
	 * @param snapshotTime settlement snapshot time in yyyyMMddHHmmss format
	 * @return direct-line total, max-line, and community weights
	 */
	private LineWeight calculateLineWeight(List<UserInfo> directUsers, Long snapshotTime) {
		if (CollectionUtil.isEmpty(directUsers)) {
			return new LineWeight(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		}
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal max = BigDecimal.ZERO;
		int effectiveLineCount = 0;
		for (UserInfo directUser : directUsers) {
			BigDecimal lineWeight = nvl(baseMapper.selectLineValidWeight(directUser.getUserId(), snapshotTime));
			if (lineWeight.compareTo(BigDecimal.ZERO) > 0) {
				effectiveLineCount++;
			}
			total = total.add(lineWeight);
			if (lineWeight.compareTo(max) > 0) {
				max = lineWeight;
			}
		}
		BigDecimal community = effectiveLineCount <= 1 ? BigDecimal.ZERO : total.subtract(max);
		return new LineWeight(
			total.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew),
			max.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew),
			community.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)
		);
	}

	private BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	public static Long weekStartTimeOf(Long time) {
		LocalDate date = parseTime(time).toLocalDate();
		LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		return formatTime(monday.atStartOfDay());
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

	private static class LineWeight {
		private final BigDecimal totalLineWeight;
		private final BigDecimal maxLineWeight;
		private final BigDecimal communityWeight;

		private LineWeight(BigDecimal totalLineWeight, BigDecimal maxLineWeight, BigDecimal communityWeight) {
			this.totalLineWeight = totalLineWeight;
			this.maxLineWeight = maxLineWeight;
			this.communityWeight = communityWeight;
		}
	}
}
