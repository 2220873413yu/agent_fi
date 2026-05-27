package com.xms.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.DemoPledgeOrder;
import com.xms.dao.domain.RewardRecord;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.service.IDemoPledgeOrderService;
import com.xms.dao.service.IRewardRecordService;
import com.xms.web.service.IDemoPledgeTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 示例质押每日收益定时任务业务实现。
 */
@Slf4j
@Service
public class DemoPledgeTaskServiceImpl implements IDemoPledgeTaskService {
	private static final int TASK_LIMIT = 1000;
	private static final int WALLET_BATCH_SIZE = 1000;
	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
	private static final String SQL_VALID_NUM1 =
		"UPDATE t_user_money SET update_time=?,gt_id=?,valid_num1=valid_num1+?,source_code=?,source_type=?,source_id=? WHERE id=? ";

	private final IDemoPledgeOrderService demoPledgeOrderService;
	private final IRewardRecordService rewardRecordService;
	private final JdbcTemplate jdbcTemplate;

	public DemoPledgeTaskServiceImpl(IDemoPledgeOrderService demoPledgeOrderService,
									 IRewardRecordService rewardRecordService,
									 JdbcTemplate jdbcTemplate) {
		this.demoPledgeOrderService = demoPledgeOrderService;
		this.rewardRecordService = rewardRecordService;
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 发放示例质押订单每日收益。
	 *
	 * <p>该任务按服务器日期yyyyMMdd做幂等控制：每个订单每天最多释放一次。
	 * 任务先抢占订单释放进度，再收集钱包入账和奖励记录，最后按1000条批量flush；
	 * 如果钱包或奖励记录落库失败，当前事务会回滚订单释放进度。</p>
	 *
	 * @return 本次成功推进释放进度的订单数量
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int releaseDailyReward() {
		Date now = new Date();
		int rewardDay = Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
		List<DemoPledgeOrder> orders = demoPledgeOrderService.lambdaQuery()
			.eq(DemoPledgeOrder::getStatus, DemoPledgeOrder.STATUS_COMPLETED)
			.eq(DemoPledgeOrder::getDeleted, 0)
			.and(wrapper -> wrapper.isNull(DemoPledgeOrder::getRewardStatus)
				.or()
				.eq(DemoPledgeOrder::getRewardStatus, DemoPledgeOrder.REWARD_STATUS_RELEASING))
			.orderByAsc(DemoPledgeOrder::getId)
			.last("limit " + TASK_LIMIT)
			.list();
		if (CollectionUtil.isEmpty(orders)) {
			return 0;
		}

		int releasedCount = 0;
		List<UserMoney> userMoneyList = new ArrayList<>(Math.min(orders.size(), WALLET_BATCH_SIZE));
		List<RewardRecord> rewardRecordList = new ArrayList<>(Math.min(orders.size(), WALLET_BATCH_SIZE));
		for (DemoPledgeOrder order : orders) {
			if (!canRelease(order)) {
				continue;
			}
			BigDecimal rewardAmount = calculateRewardAmount(order);
			boolean claimed = claimRewardProgress(order, rewardAmount, rewardDay, now);
			if (!claimed) {
				continue;
			}
			releasedCount++;
			if (rewardAmount.compareTo(BigDecimal.ZERO) > 0) {
				String gtId = IDUtils.getSnowflakeStr();
				userMoneyList.add(buildWalletIncrement(order, rewardAmount, gtId, now));
				rewardRecordList.add(buildRewardRecord(order, rewardAmount, gtId, now));
			}
			if (userMoneyList.size() >= WALLET_BATCH_SIZE) {
				flushRewardBatch(userMoneyList, rewardRecordList);
			}
		}
		flushRewardBatch(userMoneyList, rewardRecordList);
		return releasedCount;
	}

	/**
	 * 判断订单是否仍需要释放收益。
	 *
	 * @param order 示例质押订单
	 * @return true 表示释放天数未完成
	 */
	private boolean canRelease(DemoPledgeOrder order) {
		if (order.getReleaseDays() == null || order.getReleaseDays() <= 0) {
			return false;
		}
		int releasedDays = order.getReleasedDays() == null ? 0 : order.getReleasedDays();
		return releasedDays < order.getReleaseDays();
	}

	/**
	 * 计算单笔订单每日收益金额。
	 *
	 * @param order 示例质押订单
	 * @return 每日收益USDT金额，保留项目资金精度
	 */
	private BigDecimal calculateRewardAmount(DemoPledgeOrder order) {
		BigDecimal amount = defaultAmount(order.getPledgeUsdtAmount());
		BigDecimal dailyRate = defaultAmount(order.getDailyRate());
		if (amount.compareTo(BigDecimal.ZERO) <= 0 || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
			return BigDecimal.ZERO.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		}
		return amount.multiply(dailyRate)
			.divide(ONE_HUNDRED, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	/**
	 * 抢占订单当天释放进度。
	 *
	 * <p>通过 releasedDays 和 lastRewardDay 条件保证并发或重复任务不会重复发放同一天收益。
	 * 此处只推进进度，钱包和奖励记录由后续批量flush完成，失败时同事务回滚。</p>
	 *
	 * @param order 示例质押订单
	 * @param rewardAmount 本次释放收益金额
	 * @param rewardDay 释放日期yyyyMMdd
	 * @param now 当前任务时间
	 * @return true 表示抢占成功
	 */
	private boolean claimRewardProgress(DemoPledgeOrder order, BigDecimal rewardAmount, int rewardDay, Date now) {
		int currentReleasedDays = order.getReleasedDays() == null ? 0 : order.getReleasedDays();
		int nextReleasedDays = currentReleasedDays + 1;
		int nextRewardStatus = nextReleasedDays >= order.getReleaseDays()
			? DemoPledgeOrder.REWARD_STATUS_FINISHED
			: DemoPledgeOrder.REWARD_STATUS_RELEASING;
		BigDecimal nextTotalReward = defaultAmount(order.getTotalRewardUsdtAmount()).add(rewardAmount);
		return demoPledgeOrderService.lambdaUpdate()
			.eq(DemoPledgeOrder::getId, order.getId())
			.eq(DemoPledgeOrder::getStatus, DemoPledgeOrder.STATUS_COMPLETED)
			.eq(DemoPledgeOrder::getDeleted, 0)
			.eq(DemoPledgeOrder::getReleasedDays, currentReleasedDays)
			.and(wrapper -> wrapper.isNull(DemoPledgeOrder::getRewardStatus)
				.or()
				.eq(DemoPledgeOrder::getRewardStatus, DemoPledgeOrder.REWARD_STATUS_RELEASING))
			.and(wrapper -> wrapper.isNull(DemoPledgeOrder::getLastRewardDay)
				.or()
				.lt(DemoPledgeOrder::getLastRewardDay, rewardDay))
			.set(DemoPledgeOrder::getReleasedDays, nextReleasedDays)
			.set(DemoPledgeOrder::getRewardStatus, nextRewardStatus)
			.set(DemoPledgeOrder::getTotalRewardUsdtAmount, nextTotalReward)
			.set(DemoPledgeOrder::getLastRewardDay, rewardDay)
			.set(DemoPledgeOrder::getLastRewardTime, now)
			.set(DemoPledgeOrder::getUpdateTime, now)
			.update();
	}

	/**
	 * 构造USDT钱包入账增量。
	 *
	 * @param order 示例质押订单
	 * @param rewardAmount 本次收益USDT金额
	 * @param gtId 钱包追踪ID
	 * @param now 当前任务时间
	 * @return 用户钱包增量对象
	 */
	private UserMoney buildWalletIncrement(DemoPledgeOrder order, BigDecimal rewardAmount, String gtId, Date now) {
		UserMoney userMoney = new UserMoney();
		userMoney.setId(order.getUserId());
		userMoney.setValidNum1(rewardAmount);
		userMoney.setGtId(gtId);
		userMoney.setSourceCode(order.getOrderNo());
		userMoney.setSourceType(ConstantType.user_money_log_source_type.type_47);
		userMoney.setSourceId(order.getId());
		userMoney.setUpdateTime(now);
		return userMoney;
	}

	/**
	 * 构造每日收益奖励记录。
	 *
	 * @param order 示例质押订单
	 * @param rewardAmount 本次收益USDT金额
	 * @param gtId 钱包追踪ID
	 * @param now 当前任务时间
	 * @return 奖励记录
	 */
	private RewardRecord buildRewardRecord(DemoPledgeOrder order, BigDecimal rewardAmount, String gtId, Date now) {
		RewardRecord record = new RewardRecord();
		record.setOrderCode(IDUtils.getSnowflakeStr());
		record.setUserId(order.getUserId());
		record.setAmount(rewardAmount);
		record.setCoinType(ConstantType.reward_record_coin_type.type_2);
		record.setSourceType(ConstantType.xms_reward_record_source_type.type_33);
		record.setSourceOrderCode(order.getOrderNo());
		record.setSourceUserId(order.getUserId());
		record.setGtId(gtId);
		record.setCreateTime(now);
		record.setUpdateTime(now);
		return record;
	}

	/**
	 * 批量落库钱包入账和奖励记录。
	 *
	 * @param userMoneyList USDT钱包入账列表
	 * @param rewardRecordList 奖励记录列表
	 */
	private void flushRewardBatch(List<UserMoney> userMoneyList, List<RewardRecord> rewardRecordList) {
		if (CollectionUtil.isNotEmpty(userMoneyList)) {
			batchUpdateMoneyValid1(userMoneyList);
			userMoneyList.clear();
		}
		if (CollectionUtil.isNotEmpty(rewardRecordList)) {
			rewardRecordService.saveBatch(rewardRecordList);
			rewardRecordList.clear();
		}
	}

	/**
	 * 批量增加用户USDT可用余额。
	 *
	 * @param userMoneyList 钱包增量列表
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
			log.error("Demo pledge USDT reward batch update failed");
			throw new ServiceException("Demo pledge USDT reward batch update failed");
		}
	}

	/**
	 * 金额空值按0处理。
	 *
	 * @param amount 金额
	 * @return 非空金额
	 */
	private BigDecimal defaultAmount(BigDecimal amount) {
		return amount == null ? BigDecimal.ZERO : amount;
	}
}
