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
import java.util.LinkedHashSet;
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
	public static final int STATUS_WAIT = 0;
	public static final int STATUS_RUNNING = 1;
	public static final int STATUS_FINISHED = 2;
	public static final int STATUS_PAUSED = 3;
	public static final int WEEKLY_STATUS_WAIT = 0;
	public static final int WEEKLY_STATUS_QUEUED = 1;
	public static final int WEEKLY_STATUS_DONE = 3;
	public static final int G7_STATUS_WAIT = 0;
	private static final int DAILY_WAIT_PAY_LIMIT = 10;
	private static final String WEEKLY_SKIP_EXPIRED_IN_WEEK = "本周内到期，不计入周新增业绩";

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
	 * 查询后台托管订单列表展示数据。
	 *
	 * <p>列表和导出返回DTO，附带 AFI 质押比例和加速倍率快照，避免后台接口直接使用数据库实体作为展示对象。</p>
	 *
	 * @param query 查询条件
	 * @return 后台托管订单列表
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

	/**
	 * 确认用户购买托管订单的链上支付结果。
	 *
	 * <p>该方法由HTTP回调/接口触发，可能重复调用，也可能和其他业务请求乱序到达。
	 * 只有订单仍处于待支付、未开始状态时，才会把订单推进到支付成功和产出中；
	 * 后续个人/团队托管业绩、G7每日新增业绩、周新增业绩消息和等级重算消息都属于本次支付确认后的副作用。</p>
	 *
	 * @param orderNo 托管订单号
	 * @param payHash 链上支付hash
	 * @param payAmount 链上实际支付USDT金额
	 * @return 1表示回调处理完成或已幂等处理
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int confirmChainPaid(String orderNo, String payHash, BigDecimal payAmount) {
		// 1. 校验HTTP回调入参，金额必须大于0，避免错误回调推进订单状态。
		if (StrUtil.isBlank(orderNo) || StrUtil.isBlank(payHash)) {
			throw new ServiceException("订单号和支付hash不能为空");
		}
		if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("支付金额必须大于0");
		}
		// 2. 根据订单号查询本地托管订单；链上回调可能早于本地查询或重复通知，查不到时按当前口径返回成功。
		StakeHostingOrder order = lambdaQuery()
			.eq(StakeHostingOrder::getOrderNo, orderNo)
			.one();
		if (order == null) {
			return 1;
			//throw new ServiceException("托管订单不存在");
		}
		// 3. 支付成功订单直接幂等返回，避免重复回调再次累计业绩或重复发送后续消息。
		if (PAY_SUCCESS == order.getPayStatus()) {
			return 1;
		}
		// 4. 校验链上支付金额是否覆盖托管本金；不足额不能生效托管订单。
		if (payAmount.compareTo(order.getStakeUsdtAmount()) < 0) {
			throw new ServiceException("支付金额小于托管金额");
		}
		Date now = new Date();
		// 5. 计算周新增业绩时间窗口，决定订单是否进入周新增队列或直接标记跳过。
		WeeklyPerformancePrepare weeklyPrepare = prepareWeeklyPerformance(now, order.getPackageDays());
		// 6. 用订单状态条件做原子推进，确保并发/重复HTTP回调只有一个线程能从待支付推进到产出中。
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
			.set(StakeHostingOrder::getWeeklyExpirePerformanceStatus, WEEKLY_STATUS_WAIT)
			.set(StakeHostingOrder::getWeeklyExpirePerformanceSkipReason, null)
			.set(StakeHostingOrder::getWeeklyExpirePerformanceTime, null)
			.set(StakeHostingOrder::getG7NewPerformanceStatus, G7_STATUS_WAIT)
			.set(StakeHostingOrder::getG7ExpirePerformanceStatus, G7_STATUS_WAIT)
			.set(StakeHostingOrder::getUpdateTime, now)
			.update();
		if (!update) {
			throw new ServiceException("托管订单状态已变更");
		}
		// 7. 订单生效后增加用户个人和上级团队托管业绩，并维护用户is_valid有效状态。
		addHostingPerformance(order.getUserId(), order.getStakeUsdtAmount());
		// 8. 订单生效后的G7新增、周新增、小区业绩和等级重算统一发一条队列消息，消费者内按固定顺序处理。
		sendStakeHostingEffectiveAfterCommit(order.getId());
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
		order.setPayStatus(PAY_SUCCESS);
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
		order.setWeeklyExpirePerformanceStatus(WEEKLY_STATUS_WAIT);
		order.setG7NewPerformanceStatus(G7_STATUS_WAIT);
		order.setG7ExpirePerformanceStatus(G7_STATUS_WAIT);
		order.setRemark(req.getRemark());
		if (!save(order)) {
			throw new ServiceException("后台拨付托管订单失败");
		}
		addHostingPerformance(order.getUserId(), order.getStakeUsdtAmount());
		// 后台拨付订单生效后同样只发一条后置队列消息，避免后台请求被多个耗时任务阻塞。
		sendStakeHostingEffectiveAfterCommit(order.getId());
		return 1;
	}

	/**
	 * 托管订单生效后累加用户及上级托管业绩。
	 *
	 * <p>购买订单支付回调成功、后台拨付订单创建成功都会调用本方法。除业绩累加外，
	 * 会将当前用户 `is_valid` 维护为 1，表示用户持有未完成托管订单，可参与团队奖励资格判断。</p>
	 *
	 * @param userId 生效托管订单所属用户ID
	 * @param amount 托管USDT金额
	 */
	public void addHostingPerformance(Long userId, BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		// 托管订单生效后，用户持有未完成托管订单，团队奖励资格字段同步维护为有效。
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = IFNULL(performance,0) + " + amount.toPlainString())
			.set(UserInfo::getIsValid, 1)
			.set(UserInfo::getUpdateTime, new Date())
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
			// 小区业绩重算放到等级重算队列统一处理，避免支付回调同步遍历所有上级直推区。
		}
	}

	@Override
	public void subtractHostingPerformance(Long userId, BigDecimal amount) {
		subtractHostingPerformance(userId, amount, null);
	}

	/**
	 * 托管订单完成后扣减用户及上级托管业绩。
	 *
	 * <p>101 任务在订单达到套餐天数、状态更新为已完成后调用本方法。本方法只处理业绩扣减；
	 * `is_valid` 和等级重算需要等本轮101所有订单状态更新完成后，由任务统一处理，避免同一用户多笔订单同批次完成时提前判断。</p>
	 *
	 * @param userId 完成托管订单所属用户ID
	 * @param amount 扣减的托管USDT金额
	 * @param orderId 完成的托管订单ID，用于后续等级和周业绩重算消息
	 */
	@Override
	public void subtractHostingPerformance(Long userId, BigDecimal amount, Long orderId) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		// 托管订单完成后扣减个人托管业绩；is_valid 和等级重算等101批次内所有订单状态更新完后统一处理。
		userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = GREATEST(IFNULL(performance,0) - " + amount.toPlainString() + ", 0)")
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
				.update();
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
		markWeeklyExpirePerformanceQueued(recalculateOrderId);
		sendStakeHostingWeeklyExpirePerformanceAfterCommit(recalculateOrderId);
	}

	/**
	 * 按用户剩余未完成托管订单刷新团队奖励有效用户标识。
	 *
	 * <p>`t_user_info.is_valid` 本批定义为：是否持有支付成功且未完成的托管订单。
	 * 只在订单达到套餐天数并完成后重新判断，不使用 `is_return_principal` 回本状态。</p>
	 *
	 * @param userId 需要刷新的用户ID
	 */
	@Override
	public void refreshUserValidByUnfinishedHostingOrder(Long userId) {
		if (userId == null) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		// 101批次内所有完成订单状态都更新后，再判断用户是否仍有支付成功且未完成的托管订单。
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
			throw new ServiceException("刷新托管有效用户状态失败");
		}
	}

	/**
	 * 同步重算托管订单相关用户的小区业绩和真实等级。
	 *
	 * <p>该方法由Redis消费者执行实际重算。购买回调、后台拨付和101订单完成只负责发送队列消息，
	 * 避免HTTP回调或后台请求同步遍历上级链路。</p>
	 *
	 * @param orderId 用于定位下单用户及其上级链路的托管订单ID
	 */
	@Override
	public void recalculateStakeHostingLevel(Long orderId) {
		if (orderId == null) {
			return;
		}
		// 1. 只处理已支付成功的托管订单，未支付或不存在的订单不参与等级重算。
		StakeHostingOrder order = lambdaQuery()
			.eq(StakeHostingOrder::getId, orderId)
			.eq(StakeHostingOrder::getPayStatus, PAY_SUCCESS)
			.one();
		if (order == null) {
			return;
		}
		// 2. 找到下单用户，并把用户本人和所有上级都纳入本次重算范围。
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
		// 3. 先重算小区业绩，因为真实等级判断依赖个人托管业绩和小区托管业绩。
		stakeOrderService.calculateCommunityPerformance(new ArrayList<>(recalculateUserIds));
		// 4. 一次性加载等级配置，再逐个用户刷新真实等级，避免每个用户重复查配置表。
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
	 * 事务提交后发送托管等级重算消息。
	 *
	 * <p>等级重算需要重算小区业绩并遍历上级链路，不能压在HTTP回调或后台拨付请求里同步执行。</p>
	 *
	 * @param orderId 托管订单ID
	 */
	@Override
	public void sendStakeHostingLevelRecalculateAfterCommit(Long orderId) {
		sendStakeHostingOrderMessageAfterCommit(orderId, 4);
	}

	/**
	 * 事务提交后发送周新增业绩顺序消费消息。
	 *
	 * <p>周新增业绩需要顺序消费，这里只负责发送明确的 bizType=5 消息。</p>
	 *
	 * @param orderId 托管订单ID
	 */
	private void sendStakeHostingWeeklyPerformanceAfterCommit(Long orderId) {
		sendStakeHostingOrderMessageAfterCommit(orderId, 5);
	}

	/**
	 * 将订单到期周业绩重算状态标记为队列中。
	 *
	 * <p>101任务把订单更新为已完成并扣减托管业绩后调用。该状态只表示“到期导致的小区快照重算”，
	 * 不影响订单生效时的 `weekly_performance_status` 新增处理状态。</p>
	 *
	 * @param orderId 已完成的托管订单ID
	 */
	private void markWeeklyExpirePerformanceQueued(Long orderId) {
		if (orderId == null) {
			return;
		}
		lambdaUpdate()
			.eq(StakeHostingOrder::getId, orderId)
			.and(wrapper -> wrapper.isNull(StakeHostingOrder::getWeeklyExpirePerformanceStatus)
				.or()
				.ne(StakeHostingOrder::getWeeklyExpirePerformanceStatus, WEEKLY_STATUS_DONE))
			.set(StakeHostingOrder::getWeeklyExpirePerformanceStatus, WEEKLY_STATUS_QUEUED)
			.set(StakeHostingOrder::getWeeklyExpirePerformanceSkipReason, null)
			.set(StakeHostingOrder::getWeeklyExpirePerformanceTime, null)
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
	}

	/**
	 * 事务提交后发送托管订单到期周业绩重算消息。
	 *
	 * <p>bizType=7 专门用于订单到期完成后的周小区业绩快照重算，和订单生效时的 bizType=5 新增处理分开。</p>
	 *
	 * @param orderId 托管订单ID
	 */
	private void sendStakeHostingWeeklyExpirePerformanceAfterCommit(Long orderId) {
		sendStakeHostingOrderMessageAfterCommit(orderId, 7);
	}

	/**
	 * 事务提交后发送托管订单生效后置处理消息。
	 *
	 * <p>订单生效后的G7团队新增、周新增、小区业绩和等级重算属于同一条业务链路，只发送一条 bizType=6 消息，
	 * 由消费者按固定顺序处理，避免同一订单连续入队多条后置任务。</p>
	 *
	 * @param orderId 托管订单ID
	 */
	private void sendStakeHostingEffectiveAfterCommit(Long orderId) {
		sendStakeHostingOrderMessageAfterCommit(orderId, 6);
	}

	/**
	 * 事务提交后发送托管订单后置处理消息。
	 *
	 * <p>该方法只注册 afterCommit 回调并调用现有Redis/MQ生产者，不创建线程，也不使用本地 Runnable.run() 伪异步。</p>
	 *
	 * @param orderId 托管订单ID
	 * @param bizType Redis业务类型
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
	 * 发送单笔托管订单后置处理消息。
	 *
	 * @param orderId 托管订单ID
	 * @param bizType Redis业务类型
	 */
	private void sendStakeHostingOrderMessage(Long orderId, Integer bizType) {
		List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
		OrderMsgDO orderMsgDO = new OrderMsgDO();
		orderMsgDO.setId(orderId);
		orderMsgDO.setBizType(bizType);
		orderMsgDOList.add(orderMsgDO);
		asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
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

	/**
	 * 构建托管订单基础快照。
	 *
	 * <p>套餐服务费比例和全球分红套餐权重都会在下单/后台拨付时写入订单快照；
	 * 后续后台调整套餐配置，不影响历史订单的服务费、周新增小区业绩积分和全球分红权重。</p>
	 *
	 * @param userInfo 下单用户
	 * @param hostingPackage 当前命中的托管套餐配置
	 * @param amount 托管USDT金额
	 * @param createDay 创建日期，格式yyyyMMdd
	 * @return 待保存的托管订单
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
			throw new ServiceException("托管套餐业绩积分系数未配置");
		}
		// 全球分红使用“托管金额 × 套餐权重”的订单积分；1天套餐权重为0，必须按套餐配置快照写入。
		BigDecimal performanceCoefficient = hostingPackage.getPerformanceCoefficient();
		order.setPerformanceCoefficient(performanceCoefficient.setScale(4, ConstantStatic.roundingModeNew));
		order.setPerformancePoints(order.getStakeUsdtAmount().multiply(performanceCoefficient)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		order.setRunDays(0);
		order.setTodayReward(BigDecimal.ZERO);
		order.setTotalStaticReward(BigDecimal.ZERO);
		order.setIsReturnPrincipal(0);
		order.setAfiAccelerated(0);
		order.setWeeklyPerformanceStatus(WEEKLY_STATUS_WAIT);
		order.setWeeklyExpirePerformanceStatus(WEEKLY_STATUS_WAIT);
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
