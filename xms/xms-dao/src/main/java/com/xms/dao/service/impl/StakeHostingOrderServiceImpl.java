package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.StakeHostingPackage;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.mapper.StakeHostingOrderMapper;
import com.xms.dao.service.IStakeHostingPackageService;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IStakeOrderService;
import com.xms.dao.service.UserInfoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 托管订单Service业务层处理
 *
 * @author xms
 */
@Service
@AllArgsConstructor
public class StakeHostingOrderServiceImpl extends XmsDataServiceImpl<StakeHostingOrderMapper, StakeHostingOrder> implements IStakeHostingOrderService {
	public static final int SOURCE_USER = 0;
	public static final int SOURCE_ADMIN = 1;
	public static final int PAY_WAIT = 0;
	public static final int PAY_SUCCESS = 1;
	public static final int PAY_ADMIN = 2;
	public static final int STATUS_WAIT = 0;
	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_FINISHED = 2;
	public static final int STATUS_PAUSED = 3;
	public static final int WEEKLY_STATUS_WAIT = 0;
	public static final int WEEKLY_STATUS_QUEUED = 1;
	public static final int WEEKLY_STATUS_DONE = 3;
	private static final int DAILY_WAIT_PAY_LIMIT = 10;
	private static final String WEEKLY_SKIP_EXPIRED_IN_WEEK = "本周内到期，不计入周新增业绩";

	private final IStakeHostingPackageService stakeHostingPackageService;
	private final UserInfoService userInfoService;
	private final IStakeOrderService stakeOrderService;
	private final AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;

	@Override
	public List<StakeHostingOrder> selectStakeHostingOrderList(StakeHostingOrder stakeHostingOrder) {
		return baseMapper.selectStakeHostingOrderList(stakeHostingOrder);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_STAKE_APPLY, param = "#userId")
	public StakeHostingOrder createUserOrder(Long userId, Long packageId, BigDecimal amount) {
		UserInfo userInfo = getUserInfo(userId);
		StakeHostingPackage hostingPackage = getEnabledPackage(packageId);
		validateAmount(amount, hostingPackage);
		int createDay = Integer.parseInt(DateUtil.format(DateUtil.date(), "yyyyMMdd"));
		Long todayWaitCount = lambdaQuery()
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getPayStatus, PAY_WAIT)
			.eq(StakeHostingOrder::getCreateDay, createDay)
			.count();
		if (todayWaitCount >= DAILY_WAIT_PAY_LIMIT) {
			throw new ServiceException("每天最多创建10笔待支付托管订单");
		}
		StakeHostingOrder order = buildBaseOrder(userInfo, hostingPackage, amount, createDay);
		order.setSourceType(SOURCE_USER);
		order.setPayStatus(PAY_WAIT);
		order.setStatus(STATUS_WAIT);
		if (!save(order)) {
			throw new ServiceException("创建托管订单失败");
		}
		return order;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int confirmChainPaid(String orderNo, String payHash, BigDecimal payAmount) {
		if (StrUtil.isBlank(orderNo) || StrUtil.isBlank(payHash)) {
			throw new ServiceException("订单号和支付hash不能为空");
		}
		if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("支付金额必须大于0");
		}
		StakeHostingOrder order = lambdaQuery()
			.eq(StakeHostingOrder::getOrderNo, orderNo)
			.one();
		if (order == null) {
			throw new ServiceException("托管订单不存在");
		}
		if (PAY_SUCCESS == order.getPayStatus() || PAY_ADMIN == order.getPayStatus()) {
			return 1;
		}
		if (payAmount.compareTo(order.getStakeUsdtAmount()) < 0) {
			throw new ServiceException("支付金额小于托管金额");
		}
		Date now = new Date();
		WeeklyPerformancePrepare weeklyPrepare = prepareWeeklyPerformance(now, order.getPackageDays());
		boolean update = lambdaUpdate()
			.eq(StakeHostingOrder::getId, order.getId())
			.eq(StakeHostingOrder::getPayStatus, PAY_WAIT)
			.eq(StakeHostingOrder::getStatus, STATUS_WAIT)
			.set(StakeHostingOrder::getPayStatus, PAY_SUCCESS)
			.set(StakeHostingOrder::getStatus, STATUS_RUNNING)
			.set(StakeHostingOrder::getPayHash, payHash)
			.set(StakeHostingOrder::getPayAmount, payAmount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew))
			.set(StakeHostingOrder::getPayTime, now)
			.set(StakeHostingOrder::getEffectiveTime, now)
			.set(StakeHostingOrder::getPerformanceStartTime, weeklyPrepare.startTime)
			.set(StakeHostingOrder::getPerformanceEndTime, weeklyPrepare.endTime)
			.set(StakeHostingOrder::getWeeklyPerformanceStatus, weeklyPrepare.status)
			.set(StakeHostingOrder::getWeeklyPerformanceSkipReason, weeklyPrepare.skipReason)
			.set(StakeHostingOrder::getWeeklyPerformanceTime, weeklyPrepare.done ? now : null)
			.set(StakeHostingOrder::getUpdateTime, now)
			.update();
		if (!update) {
			throw new ServiceException("托管订单状态已变更");
		}
		addHostingPerformance(order.getUserId(), order.getStakeUsdtAmount());
		if (weeklyPrepare.shouldSend) {
			sendStakeHostingWeeklyPerformanceAfterCommit(order.getId());
		}
		sendStakeHostingLevelRecalculateAfterCommit(order.getId());
		return 1;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_STAKE_APPLY, param = "#req.userId")
	public int createAdminGrantOrder(StakeHostingOrder req) {
		if (req == null) {
			throw new ServiceException("拨付参数不能为空");
		}
		UserInfo userInfo = getGrantUser(req);
		StakeHostingPackage hostingPackage = getEnabledPackage(req.getPackageId());
		validateAmount(req.getStakeUsdtAmount(), hostingPackage);
		Date now = new Date();
		int createDay = Integer.parseInt(DateUtil.format(DateUtil.date(), "yyyyMMdd"));
		StakeHostingOrder order = buildBaseOrder(userInfo, hostingPackage, req.getStakeUsdtAmount(), createDay);
		order.setSourceType(SOURCE_ADMIN);
		order.setPayStatus(PAY_ADMIN);
		order.setStatus(STATUS_RUNNING);
		order.setPayAmount(req.getStakeUsdtAmount().setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		order.setPayTime(now);
		order.setEffectiveTime(now);
		WeeklyPerformancePrepare weeklyPrepare = prepareWeeklyPerformance(now, order.getPackageDays());
		order.setPerformanceStartTime(weeklyPrepare.startTime);
		order.setPerformanceEndTime(weeklyPrepare.endTime);
		order.setWeeklyPerformanceStatus(weeklyPrepare.status);
		order.setWeeklyPerformanceSkipReason(weeklyPrepare.skipReason);
		order.setWeeklyPerformanceTime(weeklyPrepare.done ? now : null);
		order.setRemark(req.getRemark());
		if (!save(order)) {
			throw new ServiceException("后台拨付托管订单失败");
		}
		addHostingPerformance(order.getUserId(), order.getStakeUsdtAmount());
		if (weeklyPrepare.shouldSend) {
			sendStakeHostingWeeklyPerformanceAfterCommit(order.getId());
		}
		sendStakeHostingLevelRecalculateAfterCommit(order.getId());
		return 1;
	}

	public void addHostingPerformance(Long userId, BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = IFNULL(performance,0) + " + amount.toPlainString())
			.update();
		if (!update) {
			throw new ServiceException("更新个人托管业绩失败");
		}
		if (userInfo.getInviteUserId() != null) {
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.setSql("sub_performance = IFNULL(sub_performance,0) + " + amount.toPlainString())
				.update();
		}
		List<Long> parentIds = userInfo.getParentIds();
		if (CollectionUtil.isNotEmpty(parentIds)) {
			update = userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, parentIds)
				.setSql("umbrella_performance = IFNULL(umbrella_performance,0) + " + amount.toPlainString())
				.setSql("performance_mining = IFNULL(performance_mining,0) + " + amount.toPlainString())
				.update();
			if (!update) {
				throw new ServiceException("更新团队托管业绩失败");
			}
			stakeOrderService.calculateCommunityPerformance(parentIds);
		}
	}

	@Override
	public void subtractHostingPerformance(Long userId, BigDecimal amount) {
		subtractHostingPerformance(userId, amount, null);
	}

	@Override
	public void subtractHostingPerformance(Long userId, BigDecimal amount, Long orderId) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = GREATEST(IFNULL(performance,0) - " + amount.toPlainString() + ", 0)")
			.update();
		if (userInfo.getInviteUserId() != null) {
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.setSql("sub_performance = GREATEST(IFNULL(sub_performance,0) - " + amount.toPlainString() + ", 0)")
				.update();
		}
		List<Long> parentIds = userInfo.getParentIds();
		if (CollectionUtil.isNotEmpty(parentIds)) {
			userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, parentIds)
				.setSql("umbrella_performance = GREATEST(IFNULL(umbrella_performance,0) - " + amount.toPlainString() + ", 0)")
				.setSql("performance_mining = GREATEST(IFNULL(performance_mining,0) - " + amount.toPlainString() + ", 0)")
				.update();
			stakeOrderService.calculateCommunityPerformance(parentIds);
		}
		Long recalculateOrderId = orderId;
		if (recalculateOrderId == null) {
			StakeHostingOrder order = lambdaQuery()
				.eq(StakeHostingOrder::getUserId, userId)
				.orderByDesc(StakeHostingOrder::getId)
				.last("limit 1")
				.one();
			recalculateOrderId = order == null ? null : order.getId();
		}
		sendStakeHostingLevelRecalculateAfterCommit(recalculateOrderId);
	}

	private void sendStakeHostingLevelRecalculateAfterCommit(Long orderId) {
		sendStakeHostingOrderMsgAfterCommit(orderId, 4);
	}

	private void sendStakeHostingWeeklyPerformanceAfterCommit(Long orderId) {
		sendStakeHostingOrderMsgAfterCommit(orderId, 5);
	}

	private void sendStakeHostingOrderMsgAfterCommit(Long orderId, Integer bizType) {
		if (orderId == null) {
			return;
		}
		Runnable sendTask = () -> {
			List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
			OrderMsgDO orderMsgDO = new OrderMsgDO();
			orderMsgDO.setId(orderId);
			orderMsgDO.setBizType(bizType);
			orderMsgDOList.add(orderMsgDO);
			asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
		};
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					sendTask.run();
				}
			});
			return;
		}
		sendTask.run();
	}

	private WeeklyPerformancePrepare prepareWeeklyPerformance(Date startDate, Integer packageDays) {
		Long startTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.formatDate(startDate);
		int days = packageDays == null ? 0 : packageDays;
		Long endTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.plusDays(startTime, days);
		Long weekEndTime = StakeHostingWeeklyCommunityPerformanceServiceImpl.weekEndTimeOf(startTime);
		WeeklyPerformancePrepare prepare = new WeeklyPerformancePrepare();
		prepare.startTime = startTime;
		prepare.endTime = endTime;
		if (endTime <= weekEndTime) {
			prepare.status = WEEKLY_STATUS_DONE;
			prepare.skipReason = WEEKLY_SKIP_EXPIRED_IN_WEEK;
			prepare.done = true;
			return prepare;
		}
		prepare.status = WEEKLY_STATUS_QUEUED;
		prepare.shouldSend = true;
		return prepare;
	}

	private static class WeeklyPerformancePrepare {
		private Long startTime;
		private Long endTime;
		private Integer status;
		private String skipReason;
		private boolean done;
		private boolean shouldSend;
	}

	private StakeHostingOrder buildBaseOrder(UserInfo userInfo, StakeHostingPackage hostingPackage, BigDecimal amount, int createDay) {
		StakeHostingOrder order = new StakeHostingOrder();
		order.setOrderNo(IDUtils.getSnowflakeStr());
		order.setUserId(userInfo.getUserId());
		order.setAccount(userInfo.getAccount());
		order.setPackageId(hostingPackage.getId());
		order.setPackageName(hostingPackage.getName());
		order.setPackageDays(hostingPackage.getDays());
		order.setStakeUsdtAmount(amount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		order.setRunDays(0);
		order.setTodayReward(BigDecimal.ZERO);
		order.setTotalStaticReward(BigDecimal.ZERO);
		order.setIsReturnPrincipal(0);
		order.setAfiAccelerated(0);
		order.setWeeklyPerformanceStatus(WEEKLY_STATUS_WAIT);
		order.setCreateDay(createDay);
		order.setCreateTime(new Date());
		return order;
	}

	private UserInfo getGrantUser(StakeHostingOrder req) {
		if (req.getUserId() != null) {
			return getUserInfo(req.getUserId());
		}
		if (StrUtil.isNotBlank(req.getAccount())) {
			UserInfo userInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getAccount, req.getAccount())
				.one();
			if (userInfo != null) {
				return userInfo;
			}
		}
		throw new ServiceException("用户不存在");
	}

	private UserInfo getUserInfo(Long userId) {
		if (userId == null) {
			throw new ServiceException("用户ID不能为空");
		}
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw new ServiceException("用户不存在");
		}
		return userInfo;
	}

	private StakeHostingPackage getEnabledPackage(Long packageId) {
		if (packageId == null) {
			throw new ServiceException("托管套餐不能为空");
		}
		StakeHostingPackage hostingPackage = stakeHostingPackageService.lambdaQuery()
			.eq(StakeHostingPackage::getId, packageId)
			.eq(StakeHostingPackage::getStatus, 1)
			.one();
		if (hostingPackage == null) {
			throw new ServiceException("托管套餐不存在或未上架");
		}
		return hostingPackage;
	}

	private void validateAmount(BigDecimal amount, StakeHostingPackage hostingPackage) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("托管金额必须大于0");
		}
		if (amount.stripTrailingZeros().scale() > 0) {
			throw new ServiceException("托管金额必须为整数");
		}
		if (amount.compareTo(hostingPackage.getMinAmount()) < 0) {
			throw new ServiceException("托管金额不能小于起购金额");
		}
	}
}
