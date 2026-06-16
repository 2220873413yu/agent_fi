package com.xms.web.service.impl;

import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.entity.bo.StakeHostingGrantRewardSwitchBo;
import com.xms.dao.service.IStakeHostingAfiPledgeService;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IStakeHostingUserAmountSummaryService;
import com.xms.dao.service.UserWalletService;
import com.xms.dao.service.impl.StakeHostingOrderServiceImpl;
import com.xms.web.service.StakeHostingOrderAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 后台托管订单操作服务实现。
 */
@Service
public class StakeHostingOrderAdminServiceImpl implements StakeHostingOrderAdminService {
	private final IStakeHostingOrderService stakeHostingOrderService;
	private final IStakeHostingAfiPledgeService stakeHostingAfiPledgeService;
	private final IStakeHostingUserAmountSummaryService stakeHostingUserAmountSummaryService;
	private final UserWalletService userWalletService;

	public StakeHostingOrderAdminServiceImpl(IStakeHostingOrderService stakeHostingOrderService,
											 IStakeHostingAfiPledgeService stakeHostingAfiPledgeService,
											 IStakeHostingUserAmountSummaryService stakeHostingUserAmountSummaryService,
											 UserWalletService userWalletService) {
		this.stakeHostingOrderService = stakeHostingOrderService;
		this.stakeHostingAfiPledgeService = stakeHostingAfiPledgeService;
		this.stakeHostingUserAmountSummaryService = stakeHostingUserAmountSummaryService;
		this.userWalletService = userWalletService;
	}

	/**
	 * 后台取消已支付且运行中的托管订单。
	 *
	 * <p>用户购买单退还 USDT 本金，后台拨付单只标记无需退本；随后统一退还仍生效的 AFI 质押、
	 * 回退托管业绩、刷新有效用户状态，并在事务提交后触发等级重算。</p>
	 *
	 * @param orderId 托管订单ID
	 * @return 1表示取消成功或重复取消幂等成功
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int cancelHostingOrder(Long orderId) {
		if (orderId == null) {
			throw new ServiceException("托管订单ID不能为空");
		}
		StakeHostingOrder order = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getId, orderId)
			.eq(StakeHostingOrder::getDeleted, 0)
			.one();
		if (order == null) {
			throw new ServiceException("托管订单不存在");
		}
		if (isAlreadyCancelled(order)) {
			return 1;
		}
		validateCancelableOrder(order);

		Date now = new Date();
		boolean userOrder = isUserOrder(order);
		int principalReturnStatus = userOrder
			? StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_DONE
			: StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_NOT_REQUIRED;
		boolean claimed = stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, order.getId())
			.eq(StakeHostingOrder::getDeleted, 0)
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.set(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_FINISHED)
			.set(StakeHostingOrder::getFinishTime, now)
			.set(StakeHostingOrder::getPrincipalReturnStatus, principalReturnStatus)
			.set(userOrder, StakeHostingOrder::getPrincipalReturnTime, now)
			.set(StakeHostingOrder::getUpdateTime, now)
			.update();
		if (!claimed) {
			StakeHostingOrder latestOrder = stakeHostingOrderService.lambdaQuery()
				.eq(StakeHostingOrder::getId, orderId)
				.eq(StakeHostingOrder::getDeleted, 0)
				.one();
			if (latestOrder != null && isAlreadyCancelled(latestOrder)) {
				return 1;
			}
			throw new ServiceException("取消托管订单失败，请刷新后重试");
		}

		BigDecimal principalAmount = order.getStakeUsdtAmount()
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if (userOrder) {
			refundUserPrincipal(order, principalAmount);
		}
		stakeHostingAfiPledgeService.returnPledgeByOrderId(order.getId());
		stakeHostingOrderService.subtractHostingPerformance(order.getUserId(), principalAmount, order.getId());
		stakeHostingUserAmountSummaryService.decreaseAmount(principalAmount);
		stakeHostingOrderService.refreshUserValidByUnfinishedHostingOrder(order.getUserId());
		stakeHostingOrderService.sendStakeHostingLevelRecalculateAfterCommit(order.getId());
		return 1;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateGrantRewardSwitch(StakeHostingGrantRewardSwitchBo req) {
		if (req == null || req.getOrderId() == null) {
			throw new ServiceException("托管订单ID不能为空");
		}
		Integer enabled = req.getGrantRewardEnabled();
		if (enabled == null || (enabled != 0 && enabled != 1)) {
			throw new ServiceException("拨付托管收益开关值不正确");
		}
		StakeHostingOrder order = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getId, req.getOrderId())
			.eq(StakeHostingOrder::getDeleted, 0)
			.one();
		if (order == null) {
			throw new ServiceException("托管订单不存在");
		}
		if (!isAdminGrantOrder(order)) {
			throw new ServiceException("仅后台拨付托管订单支持修改收益开关");
		}
		if (order.getPayStatus() == null || order.getPayStatus() != StakeHostingOrderServiceImpl.PAY_SUCCESS
			|| order.getStatus() == null || order.getStatus() != StakeHostingOrderServiceImpl.STATUS_RUNNING) {
			throw new ServiceException("仅支持修改已支付且运行中的后台拨付托管订单");
		}

		boolean updated = stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, order.getId())
			.eq(StakeHostingOrder::getDeleted, 0)
			.eq(StakeHostingOrder::getSourceType, StakeHostingOrderServiceImpl.SOURCE_ADMIN)
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.eq(StakeHostingOrder::getStatus, StakeHostingOrderServiceImpl.STATUS_RUNNING)
			.set(StakeHostingOrder::getGrantRewardEnabled, enabled)
			.set(StakeHostingOrder::getUpdateTime, new Date())
			.update();
		if (!updated) {
			throw new ServiceException("修改拨付托管收益开关失败，请刷新后重试");
		}
		return 1;
	}

	private void validateCancelableOrder(StakeHostingOrder order) {
		if (!isUserOrder(order) && !isAdminGrantOrder(order)) {
			throw new ServiceException("托管订单来源异常");
		}
		if (order.getPayStatus() == null || order.getPayStatus() != StakeHostingOrderServiceImpl.PAY_SUCCESS
			|| order.getStatus() == null || order.getStatus() != StakeHostingOrderServiceImpl.STATUS_RUNNING) {
			throw new ServiceException("仅支持取消已支付且运行中的托管订单");
		}
		Integer principalStatus = order.getPrincipalReturnStatus();
		if (principalStatus != null && principalStatus != StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_WAIT) {
			throw new ServiceException("该托管订单本金退还状态不允许取消");
		}
		if (order.getStakeUsdtAmount() == null || order.getStakeUsdtAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException(ResponseCode.CODE_1283);
		}
	}

	private void refundUserPrincipal(StakeHostingOrder order, BigDecimal principalAmount) {
		int rows = userWalletService.handerUserMoney(principalAmount, order.getOrderNo(), order.getUserId(), order.getId(),
			ConstantType.user_money_log_source_type.type_39, ConstantType.user_money_coin_type.type_1);
		if (rows != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
	}

	private boolean isAlreadyCancelled(StakeHostingOrder order) {
		if (order == null || order.getPayStatus() == null || order.getStatus() == null
			|| order.getPayStatus() != StakeHostingOrderServiceImpl.PAY_SUCCESS
			|| order.getStatus() != StakeHostingOrderServiceImpl.STATUS_FINISHED) {
			return false;
		}
		Integer principalStatus = order.getPrincipalReturnStatus();
		return (isUserOrder(order) && principalStatus != null && principalStatus == StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_DONE)
			|| (isAdminGrantOrder(order) && principalStatus != null && principalStatus == StakeHostingOrderServiceImpl.PRINCIPAL_RETURN_NOT_REQUIRED);
	}

	private boolean isUserOrder(StakeHostingOrder order) {
		return order.getSourceType() != null && order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_USER;
	}

	private boolean isAdminGrantOrder(StakeHostingOrder order) {
		return order.getSourceType() != null && order.getSourceType() == StakeHostingOrderServiceImpl.SOURCE_ADMIN;
	}
}
