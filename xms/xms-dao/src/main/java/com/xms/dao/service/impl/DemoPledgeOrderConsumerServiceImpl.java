package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.xms.common.exception.ServiceException;
import com.xms.dao.domain.DemoPledgeLevelConfig;
import com.xms.dao.domain.DemoPledgeOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.mapper.DemoPledgeOrderMapper;
import com.xms.dao.service.IDemoPledgeLevelConfigService;
import com.xms.dao.service.IDemoPledgeOrderConsumerService;
import com.xms.dao.service.UserInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 示例质押订单异步消费业务处理。
 */
@Service
public class DemoPledgeOrderConsumerServiceImpl extends XmsDataServiceImpl<DemoPledgeOrderMapper, DemoPledgeOrder> implements IDemoPledgeOrderConsumerService {
	private final UserInfoService userInfoService;
	private final IDemoPledgeLevelConfigService demoPledgeLevelConfigService;

	public DemoPledgeOrderConsumerServiceImpl(UserInfoService userInfoService,
											  IDemoPledgeLevelConfigService demoPledgeLevelConfigService) {
		this.userInfoService = userInfoService;
		this.demoPledgeLevelConfigService = demoPledgeLevelConfigService;
	}

	/**
	 * 消费已支付示例质押订单并更新用户基础业绩。
	 *
	 * <p>消费者先把订单从已支付抢占为处理中；抢占成功后再更新个人业绩、直推业绩、团队业绩和小区业绩。
	 * 处理成功后订单置为已完成。入口抢占失败说明订单已被其他消费者处理，直接幂等跳过；
	 * 但业务副作用执行后的完成状态更新失败必须抛异常，让本事务内的业绩更新一起回滚。</p>
	 *
	 * @param orderId 示例质押订单ID
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void processPaidOrder(Long orderId) {
		if (orderId == null) {
			return;
		}
		Date now = new Date();
		boolean claimed = lambdaUpdate()
			.eq(DemoPledgeOrder::getId, orderId)
			.eq(DemoPledgeOrder::getStatus, DemoPledgeOrder.STATUS_PAID)
			.eq(DemoPledgeOrder::getDeleted, 0)
			.set(DemoPledgeOrder::getStatus, DemoPledgeOrder.STATUS_PROCESSING)
			.set(DemoPledgeOrder::getProcessTime, now)
			.set(DemoPledgeOrder::getUpdateTime, now)
			.update();
		if (!claimed) {
			return;
		}

		DemoPledgeOrder order = getById(orderId);
		UserInfo buyer = userInfoService.getById(order.getUserId());
		if (buyer == null) {
			markFailed(orderId, "购买用户不存在");
			return;
		}

		// 步骤1：购买用户增加个人业绩。
		baseMapper.increasePersonalPerformance(order.getUserId(), order.getPledgeUsdtAmount());

		Set<Long> affectedUserIds = new LinkedHashSet<>();
		affectedUserIds.add(order.getUserId());

		// 步骤2：直接上级增加直推业绩。
		if (buyer.getInviteUserId() != null) {
			baseMapper.increaseDirectPerformance(buyer.getInviteUserId(), order.getPledgeUsdtAmount());
			affectedUserIds.add(buyer.getInviteUserId());
		}

		// 步骤3：父级链所有上级增加团队业绩。
		List<Long> parentIds = buyer.getParentIds();
		if (CollectionUtil.isNotEmpty(parentIds)) {
			baseMapper.increaseTeamPerformance(parentIds, order.getPledgeUsdtAmount());
			affectedUserIds.addAll(parentIds);
		}

		// 步骤4：重算受影响用户小区业绩，示例口径为团队业绩减最大单线业绩。
		for (Long userId : affectedUserIds) {
			recalculateCommunityPerformance(userId);
		}

		// 步骤5：根据个人、团队和小区业绩升级受影响用户，等级只升不降。
		upgradeAffectedUsers(affectedUserIds);

		// 步骤6：最终状态机落库必须校验返回值，防止状态被并发改动后静默完成。
		boolean finished = lambdaUpdate()
			.eq(DemoPledgeOrder::getId, orderId)
			.eq(DemoPledgeOrder::getStatus, DemoPledgeOrder.STATUS_PROCESSING)
			.set(DemoPledgeOrder::getStatus, DemoPledgeOrder.STATUS_COMPLETED)
			.set(DemoPledgeOrder::getFinishTime, now)
			.set(DemoPledgeOrder::getUpdateTime, now)
			.update();
		if (!finished) {
			throw new ServiceException("示例质押订单完成状态更新失败");
		}
	}

	/**
	 * 按示例等级配置升级受影响用户。
	 *
	 * <p>等级计算使用购买订单沉淀到用户表的个人业绩、团队业绩和小区业绩；只把用户升级到最高满足等级，
	 * 不因为配置或业绩变化做降级，避免示例消费链路产生反向副作用。</p>
	 *
	 * @param affectedUserIds 本次订单影响到的用户ID集合
	 */
	private void upgradeAffectedUsers(Set<Long> affectedUserIds) {
		if (CollectionUtil.isEmpty(affectedUserIds)) {
			return;
		}
		List<DemoPledgeLevelConfig> configs = demoPledgeLevelConfigService.lambdaQuery()
			.eq(DemoPledgeLevelConfig::getDeleted, 0)
			.gt(DemoPledgeLevelConfig::getLevel, 0)
			.orderByAsc(DemoPledgeLevelConfig::getLevel)
			.list();
		if (CollectionUtil.isEmpty(configs)) {
			return;
		}
		configs.sort(Comparator.comparing(DemoPledgeLevelConfig::getLevel));
		for (Long userId : affectedUserIds) {
			UserInfo userInfo = userInfoService.getById(userId);
			if (userInfo == null || Integer.valueOf(1).equals(userInfo.getDeleted())) {
				continue;
			}
			int matchedLevel = matchLevel(userInfo, configs);
			int currentLevel = userInfo.getGameLevel() == null ? 0 : userInfo.getGameLevel();
			if (matchedLevel > currentLevel) {
				boolean upgraded = userInfoService.lambdaUpdate()
					.eq(UserInfo::getUserId, userId)
					.eq(UserInfo::getDeleted, 0)
					.set(UserInfo::getGameLevel, matchedLevel)
					.set(UserInfo::getUpdateTime, new Date())
					.update();
				if (!upgraded) {
					throw new ServiceException("示例质押用户等级更新失败");
				}
			}
		}
	}

	/**
	 * 匹配用户当前业绩可以达到的最高示例等级。
	 *
	 * @param userInfo 用户当前业绩快照
	 * @param configs 示例等级配置，按等级从低到高
	 * @return 最高满足等级，未满足时返回0
	 */
	private int matchLevel(UserInfo userInfo, List<DemoPledgeLevelConfig> configs) {
		BigDecimal personal = defaultAmount(userInfo.getPerformance());
		BigDecimal team = defaultAmount(userInfo.getUmbrellaPerformance());
		BigDecimal community = defaultAmount(userInfo.getCommunityPerformance());
		int matchedLevel = 0;
		for (DemoPledgeLevelConfig config : configs) {
			if (personal.compareTo(defaultAmount(config.getPerformance())) >= 0
				&& team.compareTo(defaultAmount(config.getTeamPerformance())) >= 0
				&& community.compareTo(defaultAmount(config.getCommunityPerformance())) >= 0) {
				matchedLevel = config.getLevel();
			}
		}
		return matchedLevel;
	}

	/**
	 * 按“团队业绩 - 最大单线业绩”口径重算小区业绩。
	 *
	 * @param userId 需要重算小区业绩的用户ID
	 */
	private void recalculateCommunityPerformance(Long userId) {
		UserInfo userInfo = userInfoService.getById(userId);
		if (userInfo == null) {
			return;
		}
		BigDecimal teamPerformance = defaultAmount(userInfo.getUmbrellaPerformance());
		List<UserInfo> children = baseMapper.selectDirectChildren(userId);
		BigDecimal maxLinePerformance = BigDecimal.ZERO;
		for (UserInfo child : children) {
			BigDecimal linePerformance = defaultAmount(child.getPerformance()).add(defaultAmount(child.getUmbrellaPerformance()));
			if (linePerformance.compareTo(maxLinePerformance) > 0) {
				maxLinePerformance = linePerformance;
			}
		}
		BigDecimal communityPerformance = teamPerformance.subtract(maxLinePerformance);
		if (communityPerformance.compareTo(BigDecimal.ZERO) < 0) {
			communityPerformance = BigDecimal.ZERO;
		}
		int rows = baseMapper.updateCommunityPerformance(userId, communityPerformance);
		if (rows != 1) {
			throw new ServiceException("示例质押小区业绩更新失败");
		}
	}

	/**
	 * 标记示例质押订单处理失败。
	 *
	 * <p>失败状态也属于订单状态机落库，必须从处理中状态变更并校验返回值。</p>
	 *
	 * @param orderId 订单ID
	 * @param reason 失败原因
	 */
	private void markFailed(Long orderId, String reason) {
		boolean failed = lambdaUpdate()
			.eq(DemoPledgeOrder::getId, orderId)
			.eq(DemoPledgeOrder::getStatus, DemoPledgeOrder.STATUS_PROCESSING)
			.set(DemoPledgeOrder::getStatus, DemoPledgeOrder.STATUS_FAILED)
			.set(DemoPledgeOrder::getFailReason, reason)
			.set(DemoPledgeOrder::getUpdateTime, new Date())
			.update();
		if (!failed) {
			throw new ServiceException("示例质押订单失败状态更新失败");
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
