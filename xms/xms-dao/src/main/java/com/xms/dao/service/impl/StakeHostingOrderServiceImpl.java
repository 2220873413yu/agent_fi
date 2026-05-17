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
import com.xms.dao.domain.UserLevelConfig;
import com.xms.dao.entity.dto.StakeHostingOrderListDto;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.mapper.StakeHostingOrderMapper;
import com.xms.dao.service.IStakeHostingPackageService;
import com.xms.dao.service.IStakeHostingDailyTeamPerformanceService;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IStakeOrderService;
import com.xms.dao.service.IUserLevelConfigService;
import com.xms.dao.service.UserInfoService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 *
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
	public static final int STATUS_WAIT = 0;
	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_FINISHED = 2;
	public static final int STATUS_PAUSED = 3;
	public static final int G7_STATUS_WAIT = 0;
	private static final int DAILY_WAIT_PAY_LIMIT = 10;

	private final IStakeHostingPackageService stakeHostingPackageService;
	private final IStakeHostingDailyTeamPerformanceService stakeHostingDailyTeamPerformanceService;
	private final UserInfoService userInfoService;
	private final IStakeOrderService stakeOrderService;
	private final IUserLevelConfigService userLevelConfigService;
	private final AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;

	@Override
	public List<StakeHostingOrder> selectStakeHostingOrderList(StakeHostingOrder stakeHostingOrder) {
		return baseMapper.selectStakeHostingOrderList(stakeHostingOrder);
	}

	/**
	 * Adds legacy stake amount performance for compatibility callers.
	 *
	 * <p>New stake hosting effective-order paths should call {@link #addHostingPerformance(StakeHostingOrder)}
	 * so the current global dividend weight fields are maintained together with amount performance.</p>
	 *
	 * @param userId effective order user id
	 * @param amount stake USDT amount
	 */
	@Override
	public List<StakeHostingOrderListDto> selectStakeHostingOrderDtoList(StakeHostingOrderListDto query) {
		return baseMapper.selectStakeHostingOrderDtoList(query);
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
			throw new ServiceException("Daily pending order limit exceeded");
		}
		StakeHostingOrder order = buildBaseOrder(userInfo, hostingPackage, amount, createDay);
		order.setSourceType(SOURCE_USER);
		order.setPayStatus(PAY_WAIT);
		order.setStatus(STATUS_WAIT);
		if (!save(order)) {
			throw new ServiceException("Create stake hosting order failed");
		}
		return order;
	}

	/**
	 * 确认链上支付成功并让托管订单正式生效。
	 *
	 * <p>该方法用于链上支付回调或轮询确认后的订单状态推进。它会校验支付参数和支付金额，
	 * 通过待支付状态条件更新保证幂等，订单生效后同步增加本人/团队业绩和全球分红权重，
	 * 并在事务提交后发送托管订单生效异步消息，继续处理G7团队新增、小区业绩和等级刷新。</p>
	 *
	 * @param orderNo 托管订单号
	 * @param payHash 链上支付交易哈希
	 * @param payAmount 实际支付USDT金额
	 * @return 1表示处理完成；已支付订单重复回调也返回1
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int confirmChainPaid(String orderNo, String payHash, BigDecimal payAmount) {
		// 步骤1：校验链上支付确认需要的基础参数，金额必须为正数，单位为USDT。
		if (StrUtil.isBlank(orderNo) || StrUtil.isBlank(payHash)) {
			throw new ServiceException("Order no and pay hash are required");
		}
		if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("Business processing failed");
		}

		// 步骤2：按订单号读取订单。查不到时返回成功，避免外部重复推送阻塞回调流程。
		StakeHostingOrder order = lambdaQuery()
			.eq(StakeHostingOrder::getOrderNo, orderNo)
			.one();
		if (order == null) {
			return 1;
		}

		// 步骤3：已支付订单视为幂等成功，不重复增加业绩、权重或发送异步消息。
		if (PAY_SUCCESS == order.getPayStatus()) {
			return 1;
		}

		// 步骤4：实付金额不能小于订单托管USDT金额，防止少付订单被错误激活。
		if (payAmount.compareTo(order.getStakeUsdtAmount()) < 0) {
			throw new ServiceException("Pay amount is less than stake amount");
		}
		Date now = new Date();

		// 步骤5：只允许待支付、待生效状态推进为生效中，同时记录支付哈希、实付金额和生效时间快照。
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
			.set(StakeHostingOrder::getG7NewPerformanceStatus, G7_STATUS_WAIT)
			.set(StakeHostingOrder::getG7ExpirePerformanceStatus, G7_STATUS_WAIT)
			.set(StakeHostingOrder::getUpdateTime, now)
			.update();
		if (!update) {
			throw new ServiceException("Stake hosting order status changed");
		}

		// 步骤6：订单生效后同步维护本人业绩、团队业绩和全球分红权重；该部分在当前事务内落库。
		addHostingPerformance(order);

		// 步骤7：事务提交后发送异步消息，继续处理G7团队新增、小区业绩重算和真实等级刷新。
		sendStakeHostingEffectiveAfterCommit(order.getId());
		return 1;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_STAKE_APPLY, param = "#req.userId")
	public int createAdminGrantOrder(StakeHostingOrder req) {
		if (req == null) {
			throw new ServiceException("Grant request is required");
		}
		UserInfo userInfo = getGrantUser(req);
		StakeHostingPackage hostingPackage = getEnabledPackage(req.getPackageId());
		validateAmount(req.getStakeUsdtAmount(), hostingPackage);
		Date now = new Date();
		int createDay = Integer.parseInt(DateUtil.format(DateUtil.date(), "yyyyMMdd"));
		StakeHostingOrder order = buildBaseOrder(userInfo, hostingPackage, req.getStakeUsdtAmount(), createDay);
		order.setSourceType(SOURCE_ADMIN);
		order.setPayStatus(PAY_SUCCESS);
		order.setStatus(STATUS_RUNNING);
		order.setPayAmount(req.getStakeUsdtAmount().setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		order.setPayTime(now);
		order.setEffectiveTime(now);
		order.setG7NewPerformanceStatus(G7_STATUS_WAIT);
		order.setG7ExpirePerformanceStatus(G7_STATUS_WAIT);
		order.setRemark(req.getRemark());
		if (!save(order)) {
			throw new ServiceException("Create admin grant order failed");
		}
		addHostingPerformance(order);
		// Business processing note.
		sendStakeHostingEffectiveAfterCommit(order.getId());
		return 1;
	}

	/**
	 * Adds stake amount performance and current global dividend weight after an order becomes effective.
	 *
	 * <p>Amount performance still uses the order USDT amount. Global dividend weight uses the order
	 * performance point snapshot, with a package coefficient fallback for historical orders.</p>
	 *
	 * @param order effective stake hosting order
	 */
	public void addHostingPerformance(StakeHostingOrder order) {
		if (order == null) {
			return;
		}
		BigDecimal amount = order.getStakeUsdtAmount();
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(order.getUserId());
		BigDecimal globalDividendWeight = readGlobalDividendWeight(order);
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, order.getUserId())
			.setSql("performance = IFNULL(performance,0) + " + amount.toPlainString())
			.setSql(globalDividendWeight.compareTo(BigDecimal.ZERO) > 0,
				"global_dividend_weight = IFNULL(global_dividend_weight,0) + " + globalDividendWeight.toPlainString())
			.set(UserInfo::getIsValid, 1)
			.set(UserInfo::getUpdateTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException("Update user hosting performance failed");
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
				.setSql(globalDividendWeight.compareTo(BigDecimal.ZERO) > 0,
					"global_dividend_umbrella_weight = IFNULL(global_dividend_umbrella_weight,0) + " + globalDividendWeight.toPlainString())
				.update();
			if (!update) {
				throw new ServiceException("Update team hosting performance failed");
			}
			recalculateGlobalDividendCommunityWeight(parentIds);
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	public void addHostingPerformance(Long userId, BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		// Business processing note.
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = IFNULL(performance,0) + " + amount.toPlainString())
			.set(UserInfo::getIsValid, 1)
			.set(UserInfo::getUpdateTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException("Update user hosting performance failed");
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
				throw new ServiceException("Update team hosting performance failed");
			}
			// Business processing note.
		}
	}

	@Override
	public void subtractHostingPerformance(Long userId, BigDecimal amount) {
		subtractHostingPerformance(userId, amount, null);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	@Override
	public void subtractHostingPerformance(Long userId, BigDecimal amount, Long orderId) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		StakeHostingOrder finishedOrder = orderId == null ? null : lambdaQuery()
			.eq(StakeHostingOrder::getId, orderId)
			.one();
		BigDecimal globalDividendWeight = readGlobalDividendWeight(finishedOrder);
		userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = GREATEST(IFNULL(performance,0) - " + amount.toPlainString() + ", 0)")
			.setSql(globalDividendWeight.compareTo(BigDecimal.ZERO) > 0,
				"global_dividend_weight = GREATEST(IFNULL(global_dividend_weight,0) - " + globalDividendWeight.toPlainString() + ", 0)")
			.set(UserInfo::getUpdateTime, new Date())
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
				.setSql(globalDividendWeight.compareTo(BigDecimal.ZERO) > 0,
					"global_dividend_umbrella_weight = GREATEST(IFNULL(global_dividend_umbrella_weight,0) - " + globalDividendWeight.toPlainString() + ", 0)")
				.update();
			recalculateGlobalDividendCommunityWeight(parentIds);
		}
		Long recalculateOrderId = orderId;
		if (recalculateOrderId == null) {
			StakeHostingOrder order = finishedOrder != null ? finishedOrder : lambdaQuery()
				.eq(StakeHostingOrder::getUserId, userId)
				.orderByDesc(StakeHostingOrder::getId)
				.last("limit 1")
				.one();
			recalculateOrderId = order == null ? null : order.getId();
		}
	}

	/**
	 * Reads the order weight used by the current global dividend weight fields.
	 *
	 * <p>Global dividend weight must use the order snapshot first. If historical data misses
	 * {@code performance_points}, this method falls back to {@code stake_usdt_amount * performance_coefficient};
	 * if the result is still not positive, the order does not affect current global dividend weight.</p>
	 *
	 * @param order stake hosting order
	 * @return positive global dividend weight, or zero when the order is not eligible
	 */
	private BigDecimal readGlobalDividendWeight(StakeHostingOrder order) {
		if (order == null) {
			return BigDecimal.ZERO;
		}
		if (order.getPerformancePoints() != null && order.getPerformancePoints().compareTo(BigDecimal.ZERO) > 0) {
			return order.getPerformancePoints().setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		}
		if (order.getStakeUsdtAmount() == null || order.getPerformanceCoefficient() == null) {
			return BigDecimal.ZERO;
		}
		BigDecimal weight = order.getStakeUsdtAmount().multiply(order.getPerformanceCoefficient())
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		return weight.compareTo(BigDecimal.ZERO) > 0 ? weight : BigDecimal.ZERO;
	}

	/**
	 * 按团队质押大区重新计算当前全球分红小区权重。
	 *
	 * <p>每个直推用户形成一条线。大区归属必须按质押业绩线判断：
	 * {@code performance + umbrella_performance} 最大的直推线是团队质押大区。
	 * 全球分红小区权重不是排除权重最大线，而是排除这条质押大区对应的权重线。</p>
	 *
	 * @param parentIds 直推线权重或质押业绩发生变化的上级用户ID
	 */
	private void recalculateGlobalDividendCommunityWeight(List<Long> parentIds) {
		if (CollectionUtil.isEmpty(parentIds)) {
			return;
		}
		// 批量读取受影响用户的所有直推，避免每个上级单独查询一次。
		List<UserInfo> directUsers = userInfoService.lambdaQuery()
			.in(UserInfo::getInviteUserId, parentIds)
			.eq(UserInfo::getDeleted, 0)
			.orderByAsc(UserInfo::getUserId)
			.list();
		Map<Long, List<UserInfo>> directUserMap = new HashMap<>();
		for (UserInfo directUser : directUsers) {
			if (directUser.getInviteUserId() == null) {
				continue;
			}
			directUserMap.computeIfAbsent(directUser.getInviteUserId(), key -> new ArrayList<>()).add(directUser);
		}
		Date now = new Date();
		for (Long parentId : parentIds) {
			BigDecimal totalLineWeight = BigDecimal.ZERO;
			BigDecimal maxPerformance = null;
			BigDecimal maxPerformanceLineWeight = BigDecimal.ZERO;
			List<UserInfo> directUserList = directUserMap.get(parentId);
			if (CollectionUtil.isNotEmpty(directUserList)) {
				for (UserInfo directUser : directUserList) {
					// 质押业绩线用于判断团队大区是谁；权重线用于最终计算全球分红小区权重。
					BigDecimal linePerformance = nvl(directUser.getPerformance())
						.add(nvl(directUser.getUmbrellaPerformance()));
					BigDecimal lineWeight = nvl(directUser.getGlobalDividendWeight())
						.add(nvl(directUser.getGlobalDividendUmbrellaWeight()));
					totalLineWeight = totalLineWeight.add(lineWeight);
					if (maxPerformance == null || linePerformance.compareTo(maxPerformance) > 0) {
						maxPerformance = linePerformance;
						maxPerformanceLineWeight = lineWeight;
					}
				}
			}
			BigDecimal communityWeight = totalLineWeight.subtract(maxPerformanceLineWeight)
				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			if (communityWeight.compareTo(BigDecimal.ZERO) < 0) {
				communityWeight = BigDecimal.ZERO;
			}
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, parentId)
				.set(UserInfo::getGlobalDividendCommunityWeight, communityWeight)
				.set(UserInfo::getUpdateTime, now)
				.update();
		}
	}

	/**
	 * Converts nullable weight values to zero for line-weight aggregation.
	 *
	 * @param value nullable weight value
	 * @return original value, or zero when null
	 */
	private BigDecimal nvl(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 */
	@Override
	public void refreshUserValidByUnfinishedHostingOrder(Long userId) {
		if (userId == null) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		// Business processing note.
		long unfinishedCount = lambdaQuery()
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getPayStatus, PAY_SUCCESS)
			.ne(StakeHostingOrder::getStatus, STATUS_FINISHED)
			.eq(StakeHostingOrder::getDeleted, 0)
			.count();
		int validStatus = unfinishedCount > 0 ? 1 : 0;
		if (userInfo.getIsValid() != null && userInfo.getIsValid() == validStatus) {
			return;
		}
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.set(UserInfo::getIsValid, validStatus)
			.set(UserInfo::getUpdateTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException("Refresh user valid status failed");
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	@Override
	public void recalculateStakeHostingLevel(Long orderId) {
		if (orderId == null) {
			return;
		}
		// Business processing note.
		StakeHostingOrder order = lambdaQuery()
			.eq(StakeHostingOrder::getId, orderId)
			.eq(StakeHostingOrder::getPayStatus, PAY_SUCCESS)
			.one();
		if (order == null) {
			return;
		}
		// Business processing note.
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, order.getUserId())
			.one();
		if (userInfo == null) {
			return;
		}
		LinkedHashSet<Long> recalculateUserIds = new LinkedHashSet<>();
		recalculateUserIds.add(userInfo.getUserId());
		List<Long> parentIds = userInfo.getParentIds();
		if (CollectionUtil.isNotEmpty(parentIds)) {
			recalculateUserIds.addAll(parentIds);
		}
		// Business processing note.
		stakeOrderService.calculateCommunityPerformance(new ArrayList<>(recalculateUserIds));
		// Business processing note.
		List<UserLevelConfig> userLevelConfigList = userLevelConfigService.lambdaQuery()
			.gt(UserLevelConfig::getLevel, 0)
			.orderByAsc(UserLevelConfig::getLevel)
			.list();
		List<UserInfo> userInfoList = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, recalculateUserIds)
			.list();
		for (UserInfo item : userInfoList) {
			stakeOrderService.callUserLevel(item, userLevelConfigList);
		}
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 */
	@Override
	public void sendStakeHostingLevelRecalculateAfterCommit(Long orderId) {
		sendStakeHostingOrderMessageAfterCommit(orderId, 4);
	}

	/**
	 *
	 *
	 *
	 *
	 */
	private void sendStakeHostingEffectiveAfterCommit(Long orderId) {
		sendStakeHostingOrderMessageAfterCommit(orderId, 6);
	}

	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private void sendStakeHostingOrderMessageAfterCommit(Long orderId, Integer bizType) {
		if (orderId == null) {
			return;
		}
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				sendStakeHostingOrderMessage(orderId, bizType);
			}
		});
	}

	/**
	 *
	 *
	 *
	 *
	 */
	private void sendStakeHostingOrderMessage(Long orderId, Integer bizType) {
		List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
		OrderMsgDO orderMsgDO = new OrderMsgDO();
		orderMsgDO.setId(orderId);
		orderMsgDO.setBizType(bizType);
		orderMsgDOList.add(orderMsgDO);
		asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
	}


	/**
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */
	private StakeHostingOrder buildBaseOrder(UserInfo userInfo, StakeHostingPackage hostingPackage, BigDecimal amount, int createDay) {
		StakeHostingOrder order = new StakeHostingOrder();
		order.setOrderNo(IDUtils.getSnowflakeStr());
		order.setUserId(userInfo.getUserId());
		order.setAccount(userInfo.getAccount());
		order.setPackageId(hostingPackage.getId());
		order.setPackageName(hostingPackage.getName());
		order.setPackageDays(hostingPackage.getDays());
		order.setStakeUsdtAmount(amount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		// Snapshot the package service fee ratio at order creation. Future settlement must not be affected by package config changes.
		order.setServiceFeeRatio(hostingPackage.getServiceFeeRatio() == null ? BigDecimal.ZERO : hostingPackage.getServiceFeeRatio());
		if (hostingPackage.getPerformanceCoefficient() == null) {
			throw new ServiceException("Package performance coefficient is required");
		}
		// Business processing note.
		BigDecimal performanceCoefficient = hostingPackage.getPerformanceCoefficient();
		order.setPerformanceCoefficient(performanceCoefficient.setScale(4, ConstantStatic.roundingModeNew));
		order.setPerformancePoints(order.getStakeUsdtAmount().multiply(performanceCoefficient)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		order.setRunDays(0);
		order.setTodayReward(BigDecimal.ZERO);
		order.setTotalStaticReward(BigDecimal.ZERO);
		order.setIsReturnPrincipal(0);
		order.setAfiAccelerated(0);
		order.setG7NewPerformanceStatus(G7_STATUS_WAIT);
		order.setG7ExpirePerformanceStatus(G7_STATUS_WAIT);
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
		throw new ServiceException("User not found");
	}

	private UserInfo getUserInfo(Long userId) {
		if (userId == null) {
			throw new ServiceException("User id is required");
		}
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw new ServiceException("User not found");
		}
		return userInfo;
	}

	private StakeHostingPackage getEnabledPackage(Long packageId) {
		if (packageId == null) {
			throw new ServiceException("Package id is required");
		}
		StakeHostingPackage hostingPackage = stakeHostingPackageService.lambdaQuery()
			.eq(StakeHostingPackage::getId, packageId)
			.eq(StakeHostingPackage::getStatus, 1)
			.one();
		if (hostingPackage == null) {
			throw new ServiceException("Package not found or disabled");
		}
		return hostingPackage;
	}

	private void validateAmount(BigDecimal amount, StakeHostingPackage hostingPackage) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("Stake amount must be greater than zero");
		}
		if (amount.stripTrailingZeros().scale() > 0) {
			throw new ServiceException("Stake amount must be an integer");
		}
		if (amount.compareTo(hostingPackage.getMinAmount()) < 0) {
			throw new ServiceException("Stake amount is below package minimum");
		}
	}
}
