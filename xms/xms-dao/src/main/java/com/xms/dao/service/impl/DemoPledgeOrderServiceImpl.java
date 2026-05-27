package com.xms.dao.service.impl;

import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.demo.AsyncDemoPledgeOrderService;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.DemoPledgeOrder;
import com.xms.dao.domain.DemoPledgePackage;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.req.DemoPledgeBuyReq;
import com.xms.dao.mapper.DemoPledgeOrderMapper;
import com.xms.dao.service.IDemoPledgeOrderService;
import com.xms.dao.service.IDemoPledgePackageService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.UserWalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 示例质押订单Service业务层处理。
 */
@Service
public class DemoPledgeOrderServiceImpl extends XmsDataServiceImpl<DemoPledgeOrderMapper, DemoPledgeOrder> implements IDemoPledgeOrderService {
	private final IDemoPledgePackageService demoPledgePackageService;
	private final UserInfoService userInfoService;
	private final UserWalletService userWalletService;
	private final AsyncDemoPledgeOrderService asyncDemoPledgeOrderService;

	public DemoPledgeOrderServiceImpl(IDemoPledgePackageService demoPledgePackageService,
									  UserInfoService userInfoService,
									  UserWalletService userWalletService,
									  AsyncDemoPledgeOrderService asyncDemoPledgeOrderService) {
		this.demoPledgePackageService = demoPledgePackageService;
		this.userInfoService = userInfoService;
		this.userWalletService = userWalletService;
		this.asyncDemoPledgeOrderService = asyncDemoPledgeOrderService;
	}

	@Override
	public List<DemoPledgeOrder> selectDemoPledgeOrderList(DemoPledgeOrder demoPledgeOrder) {
		return baseMapper.selectDemoPledgeOrderList(demoPledgeOrder);
	}

	/**
	 * 后台演示购买示例质押套餐。
	 *
	 * <p>该方法演示复杂订单主流程：先创建待支付订单，再通过钱包标准入口扣USDT，扣款成功后把订单改为已支付，
	 * 最后在事务提交后投递Redis Stream消息。Redis只做触发器，后续消费者仍按订单ID重新查库。</p>
	 *
	 * @param req 购买请求，包含购买用户ID和套餐ID
	 * @return 已支付订单
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public DemoPledgeOrder buy(DemoPledgeBuyReq req) {
		if (req == null || req.getUserId() == null || req.getPackageId() == null) {
			throw new ServiceException("购买用户和套餐不能为空");
		}
		UserInfo userInfo = userInfoService.getById(req.getUserId());
		if (userInfo == null || Integer.valueOf(1).equals(userInfo.getDeleted())) {
			throw new ServiceException("购买用户不存在");
		}
		DemoPledgePackage pledgePackage = demoPledgePackageService.getById(req.getPackageId());
		if (pledgePackage == null || Integer.valueOf(1).equals(pledgePackage.getDeleted())) {
			throw new ServiceException("示例质押套餐不存在");
		}
		if (!Integer.valueOf(1).equals(pledgePackage.getStatus())) {
			throw new ServiceException("示例质押套餐未启用");
		}

		Date now = new Date();
		DemoPledgeOrder order = DemoPledgeOrder.builder()
			.orderNo(IDUtils.getSnowflakeStr())
			.userId(req.getUserId())
			.packageId(pledgePackage.getId())
			.packageName(pledgePackage.getPackageName())
			.pledgeUsdtAmount(pledgePackage.getPledgeUsdtAmount())
			.releaseDays(pledgePackage.getReleaseDays())
			.dailyRate(pledgePackage.getDailyRate())
			.releasedDays(0)
			.rewardStatus(DemoPledgeOrder.REWARD_STATUS_RELEASING)
			.totalRewardUsdtAmount(BigDecimal.ZERO)
			.status(DemoPledgeOrder.STATUS_PENDING_PAY)
			.build();
		order.setCreateTime(now);
		save(order);

		// 步骤1：单用户购买扣USDT，走钱包标准入口以保留gtId/sourceCode/sourceType/sourceId。
		int rows = userWalletService.handerUserMoney(
			order.getPledgeUsdtAmount().negate(),
			order.getOrderNo(),
			order.getUserId(),
			order.getId(),
			ConstantType.user_money_log_source_type.type_46,
			ConstantType.user_money_coin_type.type_1
		);
		if (rows != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		// 步骤2：扣款成功后标记已支付，等待异步消费者处理业绩。
		order.setStatus(DemoPledgeOrder.STATUS_PAID);
		order.setPayTime(now);
		order.setUpdateTime(now);
		updateById(order);

		// 步骤3：事务提交后投递消息，避免消费者读取到未提交或已回滚订单。
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				asyncDemoPledgeOrderService.sendMessage(order.getId());
			}
		});
		return order;
	}

}
