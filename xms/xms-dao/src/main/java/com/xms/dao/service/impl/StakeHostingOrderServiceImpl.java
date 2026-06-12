package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.result.ResponseCode;
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
import com.xms.dao.service.UserWalletService;
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
	public static final int PRINCIPAL_RETURN_WAIT = 0;
	public static final int PRINCIPAL_RETURN_DONE = 1;
	public static final int PRINCIPAL_RETURN_NOT_REQUIRED = 2;
	public static final int GRANT_REWARD_MODE_LOCKED = 1;
	public static final int GRANT_REWARD_MODE_DYNAMIC_AVAILABLE = 2;
	private static final int DAILY_WAIT_PAY_LIMIT = 10;

	private final IStakeHostingPackageService stakeHostingPackageService;
	private final IStakeHostingDailyTeamPerformanceService stakeHostingDailyTeamPerformanceService;
	private final UserInfoService userInfoService;
	private final IStakeOrderService stakeOrderService;
	private final IUserLevelConfigService userLevelConfigService;
	private final AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;
	private final UserWalletService userWalletService;

	@Override
	public List<StakeHostingOrder> selectStakeHostingOrderList(StakeHostingOrder stakeHostingOrder) {
		return baseMapper.selectStakeHostingOrderList(stakeHostingOrder);
	}

	/**
	 * 查询后台托管订单列表 DTO。
	 *
	 * <p>用于后台列表页读取订单、用户和展示字段的聚合结果，具体筛选规则由 Mapper SQL 承载。</p>
	 *
	 * @param query 后台托管订单列表筛选条件
	 * @return 后台托管订单列表展示数据
	 */
	@Override
	public List<StakeHostingOrderListDto> selectStakeHostingOrderDtoList(StakeHostingOrderListDto query) {
		return baseMapper.selectStakeHostingOrderDtoList(query);
	}

	/**
	 * 创建用户侧托管待支付订单。
	 *
	 * <p>该方法只保存待链上支付订单，不扣减站内钱包。下单按用户加 Redis 锁，避免同一用户并发创建过多待支付单；
	 * 支付状态初始为待支付，业务状态初始为待生效，后续由链上支付回调推进。</p>
	 *
	 * @param userId 下单用户ID
	 * @param packageId 托管套餐ID
	 * @param amount 托管 USDT 金额，必须为正整数且不低于套餐起投金额
	 * @return 已保存的待支付托管订单
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_STAKE_APPLY, param = "#userId")
	public StakeHostingOrder createUserOrder(Long userId, Long packageId, BigDecimal amount) {
		// 校验用户、启用套餐和金额规则，并在订单中固化套餐快照。
		UserInfo userInfo = getUserInfo(userId);
		StakeHostingPackage hostingPackage = getEnabledPackage(packageId);
		validateAmount(amount, hostingPackage);

		// 控制用户当天未支付订单数量，避免重复点击或恶意刷待支付订单。
		int createDay = Integer.parseInt(DateUtil.format(DateUtil.date(), "yyyyMMdd"));
		Long todayWaitCount = lambdaQuery()
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getPayStatus, PAY_WAIT)
			.eq(StakeHostingOrder::getCreateDay, createDay)
			.count();
		if (todayWaitCount >= DAILY_WAIT_PAY_LIMIT) {
			throw new ServiceException(ResponseCode.CODE_1263);
		}

		// 用户订单保存为待支付/待生效，实际支付金额、hash 和生效时间由回调填充。
		StakeHostingOrder order = buildBaseOrder(userInfo, hostingPackage, amount, createDay);
		order.setSourceType(SOURCE_USER);
		order.setPayStatus(PAY_WAIT);
		order.setStatus(STATUS_WAIT);
		if (!save(order)) {
			throw new ServiceException(ResponseCode.CODE_1298);
		}
		return order;
	}

	/**
	 * 使用用户站内 USDT 可用余额创建已支付托管订单。
	 *
	 * <p>该方法用于新的 App 托管购买流程：在同一事务内先生成订单号、扣减用户 `valid_num1`，
	 * 扣款成功后再保存已支付且产出中的托管订单，并同步增加托管业绩、团队业绩和全球分红权重。
	 * 事务提交后再发送托管生效异步消息，避免消费者读取到未提交或已回滚的数据。</p>
	 *
	 * @param userId 下单用户ID
	 * @param packageId 托管套餐ID
	 * @param amount 托管USDT金额，必须为正整数且不低于套餐起投金额
	 * @return 已支付并产出中的托管订单
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public StakeHostingOrder createUserPaidOrder(Long userId, Long packageId, BigDecimal amount) {
		// 校验用户、启用套餐和下单金额，并固化套餐快照，避免后续配置变化影响历史订单。
		UserInfo userInfo = getUserInfo(userId);
		StakeHostingPackage hostingPackage = getEnabledPackage(packageId);
		validateAmount(amount, hostingPackage);

		Date now = new Date();
		int createDay = Integer.parseInt(DateUtil.format(DateUtil.date(), "yyyyMMdd"));
		BigDecimal stakeAmount = amount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);

		// 先生成订单号，再扣站内USDT；扣款发生在订单落库前，钱包流水主要通过 sourceCode=orderNo 追踪。
		String orderNo = IDUtils.getSnowflakeStr();
		int walletRows = userWalletService.handerUserMoney(stakeAmount.negate(), orderNo, userId, userId,
			ConstantType.user_money_log_source_type.type_46, ConstantType.user_money_coin_type.type_1);
		if (walletRows != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		// 扣款成功后创建已支付订单；后续任意业务失败都会让当前事务回滚钱包扣减和订单落库。
		StakeHostingOrder order = buildBaseOrder(userInfo, hostingPackage, stakeAmount, createDay, orderNo);
		order.setSourceType(SOURCE_USER);
		order.setPayStatus(PAY_SUCCESS);
		order.setStatus(STATUS_RUNNING);
		order.setPayAmount(stakeAmount);
		order.setPayTime(now);
		order.setEffectiveTime(now);
		order.setG7NewPerformanceStatus(G7_STATUS_WAIT);
		order.setG7ExpirePerformanceStatus(G7_STATUS_WAIT);
		if (!save(order)) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		// 订单生效后同步维护当前有效托管业绩、团队业绩和全球分红权重。
		addHostingPerformance(order);

		// Redis Stream 只作为后置处理触发器，必须等主事务提交后再投递。
		sendStakeHostingEffectiveAfterCommit(order.getId());
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
			throw new ServiceException(ResponseCode.CODE_300);
		}
		if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1003);
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
			throw new ServiceException(ResponseCode.CODE_1309);
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
			throw new ServiceException(ResponseCode.CODE_1302);
		}

		// 步骤6：订单生效后同步维护本人业绩、团队业绩和全球分红权重；该部分在当前事务内落库。
		addHostingPerformance(order);

		// 步骤7：事务提交后发送异步消息，继续处理G7团队新增、小区业绩重算和真实等级刷新。
		sendStakeHostingEffectiveAfterCommit(order.getId());
		return 1;
	}

	/**
	 * 创建后台赠送的已生效托管订单。
	 *
	 * <p>后台赠送单不经过链上支付回调，保存时直接置为已支付和运行中，并同步增加用户本人业绩、
	 * 团队业绩和全球分红权重；事务提交后再投递托管生效异步消息。</p>
	 *
	 * @param req 后台赠送订单参数，用户可用 userId 或 account 定位，金额单位为 USDT
	 * @return 1 表示创建和生效处理完成
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_STAKE_APPLY, param = "#req.userId")
	public int createAdminGrantOrder(StakeHostingOrder req) {
		if (req == null) {
			throw new ServiceException(ResponseCode.CODE_300);
		}
		// 后台赠送同样使用启用套餐和金额规则，并固化套餐快照，避免后续配置变化影响历史订单。
		UserInfo userInfo = getGrantUser(req);
		StakeHostingPackage hostingPackage = getEnabledPackage(req.getPackageId());
		validateAmount(req.getStakeUsdtAmount(), hostingPackage);
		Date now = new Date();
		int createDay = Integer.parseInt(DateUtil.format(DateUtil.date(), "yyyyMMdd"));

		// 赠送订单跳过待支付状态，直接记录实付金额、生效时间和待处理的 G7 状态。
		StakeHostingOrder order = buildBaseOrder(userInfo, hostingPackage, req.getStakeUsdtAmount(), createDay);
		order.setSourceType(SOURCE_ADMIN);
		order.setGrantRewardEnabled(0);
		order.setGrantRewardMode(resolveGrantRewardMode(req.getGrantRewardMode()));
		order.setPayStatus(PAY_SUCCESS);
		order.setStatus(STATUS_RUNNING);
		order.setPayAmount(req.getStakeUsdtAmount().setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		order.setPayTime(now);
		order.setEffectiveTime(now);
		order.setG7NewPerformanceStatus(G7_STATUS_WAIT);
		order.setG7ExpirePerformanceStatus(G7_STATUS_WAIT);
		order.setRemark(req.getRemark());
		if (!save(order)) {
			throw new ServiceException(ResponseCode.CODE_1298);
		}
		addHostingPerformance(order);
		// 事务提交后再发送托管生效消息，避免消费者读取到未提交订单。
		sendStakeHostingEffectiveAfterCommit(order.getId());
		return 1;
	}

	private int resolveGrantRewardMode(Integer grantRewardMode) {
		if (grantRewardMode == null) {
			return GRANT_REWARD_MODE_LOCKED;
		}
		if (grantRewardMode != GRANT_REWARD_MODE_LOCKED && grantRewardMode != GRANT_REWARD_MODE_DYNAMIC_AVAILABLE) {
			throw new ServiceException("后台拨付托管收益分配方式不正确");
		}
		return grantRewardMode;
	}

	/**
	 * 停止用户购买的 1 天自动复投托管订单，并立即退还 USDT 本金。
	 *
	 * <p>该方法只允许当前用户停止自己的购买单：`source_type=0`、`package_days=1`、`pay_status=1`、
	 * `status=1`、`principal_return_status=0`。订单状态使用 `status=2` 已完成，不新增停止状态。
	 * 退本写入 USDT 可用余额 `valid_num1`，流水 `source_type=39`，并在同一事务中回滚托管业绩、
	 * 刷新用户有效状态，事务提交后触发等级重算。</p>
	 *
	 * @param userId 当前登录用户ID
	 * @param orderId 托管订单ID
	 * @return 1 表示停止成功
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int stopUserOneDayAutoReinvestOrder(Long userId, Long orderId) {
		if (userId == null || orderId == null) {
			throw new ServiceException(ResponseCode.CODE_1277);
		}
		// 只读取当前用户自己的订单，避免用户端传入其他人的订单ID。
		StakeHostingOrder order = lambdaQuery()
			.eq(StakeHostingOrder::getId, orderId)
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getDeleted, 0)
			.one();
		if (order == null) {
			throw new ServiceException(ResponseCode.CODE_1299);
		}
		// 已经完成停止且本金已退还的重复请求视为成功，避免用户重复点击或网络重试造成误报。
		if (order.getSourceType() == SOURCE_USER
			&& order.getPackageDays() == 1
			&& order.getPayStatus() == PAY_SUCCESS
			&& order.getStatus() == STATUS_FINISHED
			&& order.getPrincipalReturnStatus() == PRINCIPAL_RETURN_DONE) {
			return 1;
		}
		// 只允许用户购买的 1 天自动复投订单停止；后台拨付单、非 1 天套餐、未支付/已完成订单都不能在 App 退本。
		if ( order.getSourceType() != SOURCE_USER
			|| order.getPackageDays() != 1
			|| order.getPayStatus() != PAY_SUCCESS
			|| order.getStatus() != STATUS_RUNNING) {
			throw new ServiceException(ResponseCode.CODE_1300);
		}
		if (order.getPrincipalReturnStatus() != PRINCIPAL_RETURN_WAIT) {
			throw new ServiceException(ResponseCode.CODE_1301);
		}
		if (order.getStakeUsdtAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1283);
		}

		Date now = new Date();
		BigDecimal principalAmount = order.getStakeUsdtAmount()
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		// 使用订单状态和退本状态做抢占，确保重复点击、重复请求不会重复退还 USDT 本金。
		boolean claimed = lambdaUpdate()
			.eq(StakeHostingOrder::getId, order.getId())
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getSourceType, SOURCE_USER)
			.eq(StakeHostingOrder::getPackageDays, 1)
			.eq(StakeHostingOrder::getPayStatus, PAY_SUCCESS)
			.eq(StakeHostingOrder::getStatus, STATUS_RUNNING)
			.eq(StakeHostingOrder::getPrincipalReturnStatus, PRINCIPAL_RETURN_WAIT)
			.set(StakeHostingOrder::getStatus, STATUS_FINISHED)
			.set(StakeHostingOrder::getFinishTime, now)
			.set(StakeHostingOrder::getPrincipalReturnStatus, PRINCIPAL_RETURN_DONE)
			.set(StakeHostingOrder::getPrincipalReturnTime, now)
			.set(StakeHostingOrder::getUpdateTime, now)
			.update();
		if (!claimed) {
			throw new ServiceException(ResponseCode.CODE_1302);
		}

		// 停止成功后立即退还 USDT 本金到可用余额；sourceId 使用托管订单ID，便于流水追踪和排查。
		int rows = userWalletService.handerUserMoney(principalAmount, order.getOrderNo(), order.getUserId(), order.getId(),
			ConstantType.user_money_log_source_type.type_39, ConstantType.user_money_coin_type.type_1);
		if (rows != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
		// 订单退出产出后回滚本人、上级团队和全局分红权重相关业绩。
		subtractHostingPerformance(order.getUserId(), principalAmount, order.getId());
		// 如果用户已经没有其他未完成托管单，则同步刷新为无效用户。
		refreshUserValidByUnfinishedHostingOrder(order.getUserId());
		// 等级重算放到事务提交后投递，消费者再按订单ID重新查库处理。
		sendStakeHostingLevelRecalculateAfterCommit(order.getId());
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
			throw new ServiceException(ResponseCode.CODE_1003);
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
				throw new ServiceException(ResponseCode.CODE_1003);
			}
			recalculateGlobalDividendCommunityWeight(parentIds);
		}
	}

	/**
	 * 按金额增加托管业绩的兼容入口。
	 *
	 * <p>该方法只维护旧版金额业绩字段，不维护当前全球分红权重。新托管订单生效链路应优先使用
	 * {@link #addHostingPerformance(StakeHostingOrder)}，以便同时维护订单绩效积分权重。</p>
	 *
	 * @param userId 生效订单用户ID
	 * @param amount 增加的托管 USDT 金额
	 */
	public void addHostingPerformance(Long userId, BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		// 增加本人托管业绩并标记有效用户，兼容旧链路只使用 USDT 金额。
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = IFNULL(performance,0) + " + amount.toPlainString())
			.set(UserInfo::getIsValid, 1)
			.set(UserInfo::getUpdateTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException(ResponseCode.CODE_1003);
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
				throw new ServiceException(ResponseCode.CODE_1003);
			}
			// 旧兼容入口不重算全球分红小区权重，因为没有订单绩效积分快照。
		}
	}

	/**
	 * 按金额扣减用户托管业绩。
	 *
	 * @param userId 用户ID
	 * @param amount 扣减的托管 USDT 金额
	 */
	@Override
	public void subtractHostingPerformance(Long userId, BigDecimal amount) {
		subtractHostingPerformance(userId, amount, null);
	}

	/**
	 * 托管订单结束或回退时扣减本人和团队托管业绩。
	 *
	 * <p>金额业绩按 USDT 扣减到不低于 0；如果传入订单ID，则同时按该订单的绩效积分快照扣减全球分红权重，
	 * 并重算受影响上级的全球分红小区权重。</p>
	 *
	 * @param userId 用户ID
	 * @param amount 扣减的托管 USDT 金额
	 * @param orderId 结束或回退的托管订单ID，可为空；为空时只按金额扣减
	 */
	@Override
	public void subtractHostingPerformance(Long userId, BigDecimal amount, Long orderId) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		// 订单快照用于扣减全球分红权重；没有订单时只处理旧版金额业绩。
		StakeHostingOrder finishedOrder = orderId == null ? null : lambdaQuery()
			.eq(StakeHostingOrder::getId, orderId)
			.one();
		BigDecimal globalDividendWeight = readGlobalDividendWeight(finishedOrder);

		// 本人业绩和全球分红权重扣减到不低于 0，避免历史异常数据导致负数。
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = GREATEST(IFNULL(performance,0) - " + amount.toPlainString() + ", 0)")
			.setSql(globalDividendWeight.compareTo(BigDecimal.ZERO) > 0,
				"global_dividend_weight = GREATEST(IFNULL(global_dividend_weight,0) - " + globalDividendWeight.toPlainString() + ", 0)")
			.set(UserInfo::getUpdateTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException(ResponseCode.CODE_1003);
		}
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
	 * 根据未完成托管订单刷新用户有效状态。
	 *
	 * <p>用户存在已支付且未完成的托管订单时视为有效用户，否则标记为无效。该方法通常在订单完成、
	 * 回退或状态修正后调用，只修改用户有效标记和更新时间。</p>
	 *
	 * @param userId 用户ID
	 */
	@Override
	public void refreshUserValidByUnfinishedHostingOrder(Long userId) {
		if (userId == null) {
			return;
		}
		UserInfo userInfo = getUserInfo(userId);
		// 只统计已支付且未完成的托管订单，待支付订单不参与有效用户判断。
		long unfinishedCount = lambdaQuery()
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getPayStatus, PAY_SUCCESS)
			.ne(StakeHostingOrder::getStatus, STATUS_FINISHED)
			.count();
		int validStatus = unfinishedCount > 0 ? 1 : 0;
		if (userInfo.getIsValid() == validStatus) {
			return;
		}
		 userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.set(UserInfo::getIsValid, validStatus)
			.set(UserInfo::getUpdateTime, new Date())
			.update();
	}

	/**
	 * 重算托管订单影响链路上的用户等级。
	 *
	 * <p>订单生效或业绩变化后，按订单用户及其所有上级重新计算社区业绩，再用当前等级配置刷新真实用户等级。
	 * 该方法只处理已支付订单；找不到订单或用户时幂等跳过。</p>
	 *
	 * @param orderId 触发等级重算的托管订单ID
	 */
	@Override
	public void recalculateStakeHostingLevel(Long orderId) {
		if (orderId == null) {
			return;
		}
		// 只使用已支付订单触发等级重算，待支付订单不产生业绩和等级影响。
		StakeHostingOrder order = lambdaQuery()
			.eq(StakeHostingOrder::getId, orderId)
			.eq(StakeHostingOrder::getPayStatus, PAY_SUCCESS)
			.one();
		if (order == null) {
			return;
		}
		// 以订单用户为起点，把本人和所有上级加入本次重算集合。
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
		// 先重算社区业绩，再按等级配置刷新用户等级，保证等级判断使用最新业绩。
		stakeOrderService.calculateCommunityPerformance(new ArrayList<>(recalculateUserIds));
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
	 * <p>bizType=4 由异步消费者识别为托管等级重算，消息只携带订单ID，消费者需重新查库处理。</p>
	 *
	 * @param orderId 托管订单ID
	 */
	@Override
	public void sendStakeHostingLevelRecalculateAfterCommit(Long orderId) {
		sendStakeHostingOrderMessageAfterCommit(orderId, 4);
	}

	/**
	 * 事务提交后发送托管订单生效消息。
	 *
	 * <p>bizType=6 用于触发托管订单生效后的 G7 团队业绩、新增业绩和等级刷新等后置处理。</p>
	 *
	 * @param orderId 已生效托管订单ID
	 */
	private void sendStakeHostingEffectiveAfterCommit(Long orderId) {
		sendStakeHostingOrderMessageAfterCommit(orderId, 6);
	}

	/**
	 * 注册事务提交后的托管订单异步消息。
	 *
	 * <p>Redis 消息只作为触发器，必须等当前事务提交后发送，避免消费者读取到未提交或已回滚的数据。</p>
	 *
	 * @param orderId 托管订单ID
	 * @param bizType 异步业务类型
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
	 * 发送托管订单异步业务消息。
	 *
	 * <p>消息体只放订单ID和业务类型，后续消费者以数据库订单记录为事实来源。</p>
	 *
	 * @param orderId 托管订单ID
	 * @param bizType 异步业务类型
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
	 * 构建托管订单基础快照。
	 *
	 * <p>订单创建时固化套餐名称、周期、服务费比例、绩效系数和绩效积分，后续结算、业绩和分红权重均使用订单快照，
	 * 不受后台套餐配置变更影响。该方法只填充基础字段，支付状态和业务状态由调用方按用户下单或后台赠送场景设置。</p>
	 *
	 * @param userInfo 下单或赠送目标用户
	 * @param hostingPackage 启用中的托管套餐
	 * @param amount 托管 USDT 金额
	 * @param createDay 创建日期，yyyyMMdd 数字格式
	 * @return 未保存的托管订单对象
	 */
	private StakeHostingOrder buildBaseOrder(UserInfo userInfo, StakeHostingPackage hostingPackage, BigDecimal amount, int createDay) {
		return buildBaseOrder(userInfo, hostingPackage, amount, createDay, IDUtils.getSnowflakeStr());
	}

	/**
	 * 使用指定订单号构建托管订单基础快照。
	 *
	 * <p>站内 USDT 支付流程需要先生成订单号用于钱包流水 `sourceCode`，再把同一个订单号写入托管订单；
	 * 旧链上待支付和后台拨付流程仍可通过无订单号重载自动生成订单号。</p>
	 *
	 * @param userInfo 下单或赠送目标用户
	 * @param hostingPackage 启用中的托管套餐
	 * @param amount 托管 USDT 金额
	 * @param createDay 创建日期，yyyyMMdd 数字格式
	 * @param orderNo 预生成的托管订单号
	 * @return 未保存的托管订单对象
	 */
	private StakeHostingOrder buildBaseOrder(UserInfo userInfo, StakeHostingPackage hostingPackage, BigDecimal amount, int createDay, String orderNo) {
		StakeHostingOrder order = new StakeHostingOrder();
		order.setOrderNo(orderNo);
		order.setUserId(userInfo.getUserId());
		order.setAccount(userInfo.getAccount());
		order.setPackageId(hostingPackage.getId());
		order.setPackageName(hostingPackage.getName());
		order.setPackageDays(hostingPackage.getDays());
		order.setStakeUsdtAmount(amount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		// Snapshot the package service fee ratio at order creation. Future settlement must not be affected by package config changes.
		order.setServiceFeeRatio(hostingPackage.getServiceFeeRatio() == null ? BigDecimal.ZERO : hostingPackage.getServiceFeeRatio());
		if (hostingPackage.getPerformanceCoefficient() == null) {
			throw new ServiceException(ResponseCode.CODE_1303);
		}
		// 绩效积分按订单金额 * 套餐绩效系数计算，是全球分红权重和后续业绩口径的重要快照。
		BigDecimal performanceCoefficient = hostingPackage.getPerformanceCoefficient();
		order.setPerformanceCoefficient(performanceCoefficient.setScale(4, ConstantStatic.roundingModeNew));
		order.setPerformancePoints(order.getStakeUsdtAmount().multiply(performanceCoefficient)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew));
		order.setRunDays(0);
		order.setTodayReward(BigDecimal.ZERO);
		order.setTotalStaticReward(BigDecimal.ZERO);
		order.setIsReturnPrincipal(0);
		order.setPrincipalReturnStatus(PRINCIPAL_RETURN_WAIT);
		order.setAfiAccelerated(0);
		order.setG7NewPerformanceStatus(G7_STATUS_WAIT);
		order.setG7ExpirePerformanceStatus(G7_STATUS_WAIT);
		order.setCreateDay(createDay);
		order.setCreateTime(new Date());
		return order;
	}

	/**
	 * 解析后台赠送订单的目标用户。
	 *
	 * <p>后台可优先按 userId 定位用户；未传 userId 时按钱包地址 account 查询。两者都找不到则业务失败。</p>
	 *
	 * @param req 后台赠送订单参数
	 * @return 赠送目标用户
	 */
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
		throw new ServiceException(ResponseCode.CODE_1007);
	}

	/**
	 * 查询托管业务用户并做存在性校验。
	 *
	 * @param userId 用户ID
	 * @return 用户信息
	 */
	private UserInfo getUserInfo(Long userId) {
		if (userId == null) {
			throw new ServiceException(ResponseCode.CODE_300);
		}
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw new ServiceException(ResponseCode.CODE_1007);
		}
		return userInfo;
	}

	/**
	 * 查询启用中的托管套餐。
	 *
	 * <p>只有 status=1 的套餐允许用于创建订单，订单创建后会保存套餐快照。</p>
	 *
	 * @param packageId 托管套餐ID
	 * @return 启用中的托管套餐
	 */
	private StakeHostingPackage getEnabledPackage(Long packageId) {
		if (packageId == null) {
			throw new ServiceException(ResponseCode.CODE_1304);
		}
		StakeHostingPackage hostingPackage = stakeHostingPackageService.lambdaQuery()
			.eq(StakeHostingPackage::getId, packageId)
			.eq(StakeHostingPackage::getStatus, 1)
			.one();
		if (hostingPackage == null) {
			throw new ServiceException(ResponseCode.CODE_1305);
		}
		return hostingPackage;
	}

	/**
	 * 校验托管下单金额。
	 *
	 * <p>金额单位为 USDT，必须大于 0、必须为整数，并且不低于套餐最低起投金额。</p>
	 *
	 * @param amount 托管 USDT 金额
	 * @param hostingPackage 托管套餐
	 */
	private void validateAmount(BigDecimal amount, StakeHostingPackage hostingPackage) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1306);
		}
		if (amount.stripTrailingZeros().scale() > 0) {
			throw new ServiceException(ResponseCode.CODE_1307);
		}
		if (amount.compareTo(hostingPackage.getMinAmount()) < 0) {
			throw new ServiceException(ResponseCode.CODE_1308);
		}
	}
}
