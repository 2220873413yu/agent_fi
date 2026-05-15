package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xms.dao.domain.StakeHostingGlobalDividendWeightSnapshot;
import com.xms.dao.mapper.StakeHostingGlobalDividendWeightSnapshotMapper;
import com.xms.dao.service.IStakeHostingGlobalDividendWeightSnapshotService;
import org.springframework.stereotype.Service;

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

/**
 * Business service for stake hosting global dividend weekly weight snapshots.
 */
@Service
public class StakeHostingGlobalDividendWeightSnapshotServiceImpl
	extends XmsDataServiceImpl<StakeHostingGlobalDividendWeightSnapshotMapper, StakeHostingGlobalDividendWeightSnapshot>
	implements IStakeHostingGlobalDividendWeightSnapshotService {

	private static final ZoneId BIZ_ZONE = ZoneId.of("Asia/Shanghai");
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	@Override
	public List<StakeHostingGlobalDividendWeightSnapshot> selectStakeHostingGlobalDividendWeightSnapshotList(StakeHostingGlobalDividendWeightSnapshot snapshot) {
		return baseMapper.selectStakeHostingGlobalDividendWeightSnapshotList(snapshot);
	}

	@Override
	public List<StakeHostingGlobalDividendWeightSnapshot> selectLatestBeforeWeek(Long weekStartTime) {
		return baseMapper.selectLatestBeforeWeek(weekStartTime);
	}

	@Override
	public void batchUpsert(List<StakeHostingGlobalDividendWeightSnapshot> snapshots) {
		if (CollectionUtil.isEmpty(snapshots)) {
			return;
		}
		baseMapper.batchUpsert(snapshots);
	}

	/**
	 * Locates the Monday 00:00:00 boundary of the natural week containing the given timestamp.
	 *
	 * @param time timestamp in yyyyMMddHHmmss format
	 * @return week start timestamp in yyyyMMddHHmmss format
	 */
	public static Long weekStartTimeOf(Long time) {
		LocalDate date = parseTime(time).toLocalDate();
		LocalDate monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		return formatTime(monday.atStartOfDay());
	}

	/**
	 * Locates the Sunday 23:59:59 boundary of the natural week containing the given timestamp.
	 *
	 * @param time timestamp in yyyyMMddHHmmss format
	 * @return week end timestamp in yyyyMMddHHmmss format
	 */
	public static Long weekEndTimeOf(Long time) {
		LocalDate date = parseTime(time).toLocalDate();
		LocalDate sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
		return formatTime(LocalDateTime.of(sunday, LocalTime.of(23, 59, 59)));
	}

	/**
	 * Formats a Java date into the business timestamp used by weekly dividend snapshots.
	 *
	 * @param date date in server JVM time
	 * @return timestamp in yyyyMMddHHmmss format using Asia/Shanghai
	 */
	public static Long formatDate(Date date) {
		return Long.valueOf(LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), BIZ_ZONE).format(TIME_FORMATTER));
	}

	private static LocalDateTime parseTime(Long time) {
		return LocalDateTime.parse(String.valueOf(time), TIME_FORMATTER);
	}

	private static Long formatTime(LocalDateTime time) {
		return Long.valueOf(time.format(TIME_FORMATTER));
	}
}
