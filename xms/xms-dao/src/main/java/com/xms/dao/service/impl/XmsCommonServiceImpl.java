package com.xms.dao.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.DateUtils;
import com.xms.dao.mapper.AsyncTaskMapper;
import com.xms.dao.service.ISysParaService;
import com.xms.dao.service.XmsCommonService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 2023/09/22
 *
 * @author MIER
 */
@Service
@Slf4j
@AllArgsConstructor
public class XmsCommonServiceImpl implements XmsCommonService {
	private final XmsRedis xmsRedis;
	private final ISysParaService sysParaServiceImpl;
	private final AsyncTaskMapper asyncTaskMapper;

	public static ResultPista  testCheckMineSettleTime(DateTime currentDate) {
		String miningLimit ="23:55-01:00";
		String[] strs = miningLimit.split("-");
		Date star1Date = DateUtil.parseTimeToday(strs[0]);
		Date end1Date = DateUtil.parseTimeToday(strs[1]);
		log.info("starDate:{} ,endDate:{}", star1Date, end1Date);
		//boolean res = DateUtil.isIn(currentDate, star1Date, end1Date);
		boolean res = isInTimeRange(currentDate, strs[0], strs[1]);
		if (res) {
/*			//再时间段内，如果结算处理完，就成功，否则就是无法交易
			Map<String, Object> param = new HashMap<>(4);
			param.put("type", SysConstant.TSK_TYPE_100);
			param.put("date", DateUtil.format(DateUtil.date(), "yyyyMMdd"));
			Map<String, Object> task = asyncTaskMapper.getTask(param);
			if (task != null) {
				//可以继续交易，否则的话
				return ResultPista.success();
			}*/
			return ResultPista.fail(ResponseCode.CODE_1024.getCode(), MessageFormat.format(ResponseCode.CODE_1024.getMsg(), miningLimit));
		}
		return ResultPista.success();
	}

	public static void testTime(String timeToTest, String timeRange) {
		DateTime dateTime = DateUtil.parse(timeToTest);
		String[] strs = timeRange.split("-");
		boolean res = isInTimeRange(dateTime, strs[0], strs[1]);
		System.out.println(timeToTest + " 是否在时间范围 " + timeRange + " 内: " + (res ? "在时间内" : "不在时间内"));
	}

	public static boolean isInTimeRange(DateTime dateTime, String startTime, String endTime) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
		LocalTime start = LocalTime.parse(startTime, formatter);
		LocalTime end = LocalTime.parse(endTime, formatter);

		// 将 dateTime 转换为 LocalTime
		LocalTime timeToCheck = LocalTime.of(dateTime.hour(true), dateTime.minute(), dateTime.second());

		if (end.isBefore(start)) {
			// 如果结束时间小于开始时间，说明跨越了午夜
			return !timeToCheck.isBefore(start) || timeToCheck.isBefore(end);
		} else {
			// 不跨午夜的情况
			return !timeToCheck.isBefore(start) && timeToCheck.isBefore(end);
		}
	}

	/**
	 * 系统结算时间，不允许交易
	 * @return
	 */
	@Override
	public ResultPista checkMineSettleTime() {
		DateTime currentDate = DateUtil.date();
		//miningLimit=23:59-00:30
		String miningLimit = sysParaServiceImpl.getValue(SysConstant.MINE_TRADE_TIME_LIMIT);

		String[] strs = miningLimit.split("-");
		Date star1Date = DateUtil.parseTimeToday(strs[0]);
		Date end1Date = DateUtil.parseTimeToday(strs[1]);
		log.info("starDate:{} ,endDate:{}", star1Date, end1Date);
		boolean res = isInTimeRange(currentDate, strs[0], strs[1]);
		if (res) {
			//再时间段内，如果结算处理完，就成功，否则就是无法交易
			Map<String, Object> param = new HashMap<>(4);
			param.put("type", SysConstant.TSK_TYPE_101);
			param.put("date", DateUtil.format(DateUtil.date(), "yyyyMMdd"));
			Map<String, Object> task = asyncTaskMapper.getTask(param);
			if (task != null) {
				return ResultPista.success();
			}
			return ResultPista.fail(ResponseCode.CODE_1024.getCode(), MessageFormat.format(ResponseCode.CODE_1024.getMsg(), miningLimit));
		}
		return ResultPista.success();
	}

	//测试checkMineSettleTime
	public static void main(String[] args) {
		XmsCommonServiceImpl vo = new XmsCommonServiceImpl(null, null, null);
		DateTime  curr =DateUtil.parse("2026-03-10 23:59:00");
		System.out.println(vo.checkMineSettleTime(curr,"23:59-00:30",null,null,null));
	}
	/**
	 * 可传入自定义时间/区间/任务类型进行测试（按需查询DB）
	 * @param currentDate 当前时间（可为空，默认当前时间）
	 * @param miningLimit 结算区间，如 "00:00-00:30"（可为空，默认取系统配置）
	 * @param taskType100 任务类型100（可为null）
	 * @param taskType101 任务类型101（可为null）
	 * @param dateStr 日期字符串 yyyyMMdd（可为null，默认当前日期）
	 * @return
	 */
	public ResultPista checkMineSettleTime(DateTime currentDate, String miningLimit,
										   Integer taskType100, Integer taskType101, String dateStr) {
		DateTime date = currentDate == null ? DateUtil.date() : currentDate;
		String limit = (miningLimit == null || miningLimit.trim().isEmpty())
			? sysParaServiceImpl.getValue(SysConstant.MINE_TRADE_TIME_LIMIT)
			: miningLimit;
		String[] strs = limit.split("-");
		boolean res = isInTimeRange(date, strs[0], strs[1]);
		if (res) {
			if (taskType100 == null) {
				return ResultPista.fail(ResponseCode.CODE_1024.getCode(),
					MessageFormat.format(ResponseCode.CODE_1024.getMsg(), limit));
			}
		}
		return ResultPista.success();
	}


}
