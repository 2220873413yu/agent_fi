package com.xms.dao.service.impl;

import cn.hutool.core.date.DateUtil;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.StakeHostingAfiAccelerateConfig;
import com.xms.dao.domain.StakeHostingAfiPledge;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.entity.vo.UpdateUserMoneyVo;
import com.xms.dao.entity.vo.UserMoneyLogVo;
import com.xms.dao.mapper.StakeHostingAfiPledgeMapper;
import com.xms.dao.service.IStakeHostingAfiAccelerateConfigService;
import com.xms.dao.service.IStakeHostingAfiPledgeService;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IUserMoneyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 托管订单AFI质押记录Service业务层处理
 *
 * @author xms
 */
@Service
public class StakeHostingAfiPledgeServiceImpl extends XmsDataServiceImpl<StakeHostingAfiPledgeMapper, StakeHostingAfiPledge> implements IStakeHostingAfiPledgeService {
	public static final int STATUS_WAIT_EFFECTIVE = 0;
	public static final int STATUS_EFFECTIVE = 1;
	public static final int STATUS_RETURNED = 2;
	public static final int AFI_ACCELERATED_NO = 0;
	public static final int AFI_ACCELERATED_YES = 1;
	private final IStakeHostingOrderService stakeHostingOrderService;
	private final IStakeHostingAfiAccelerateConfigService afiAccelerateConfigService;
	private final IUserMoneyService userMoneyService;

	public StakeHostingAfiPledgeServiceImpl(IStakeHostingOrderService stakeHostingOrderService,
											IStakeHostingAfiAccelerateConfigService afiAccelerateConfigService,
											IUserMoneyService userMoneyService) {
		this.stakeHostingOrderService = stakeHostingOrderService;
		this.afiAccelerateConfigService = afiAccelerateConfigService;
		this.userMoneyService = userMoneyService;
	}

	@Override
	public List<StakeHostingAfiPledge> selectStakeHostingAfiPledgeList(StakeHostingAfiPledge pledge) {
		return baseMapper.selectStakeHostingAfiPledgeList(pledge);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public StakeHostingAfiPledge pledgeAfi(Long userId, Long stakeHostingOrderId, BigDecimal afiAmount, BigDecimal afiPrice) {
		if (userId == null) {
			throw new ServiceException("用户ID不能为空");
		}
		if (stakeHostingOrderId == null) {
			throw new ServiceException("托管订单不能为空");
		}
		if (afiAmount == null || afiAmount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("AFI质押数量必须大于0");
		}
		StakeHostingOrder order = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getId, stakeHostingOrderId)
			.eq(StakeHostingOrder::getUserId, userId)
			.eq(StakeHostingOrder::getDeleted, 0)
			.one();
		validateOrder(order);

		BigDecimal afiPriceScaled = normalizeAfiPrice(afiPrice);
		BigDecimal afiAmountScaled = afiAmount.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal afiUsdtAmount = afiAmountScaled.multiply(afiPriceScaled).setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal pledgeRatio = afiUsdtAmount.multiply(new BigDecimal("100"))
			.divide(order.getStakeUsdtAmount(), 6, ConstantStatic.roundingModeNew);
		StakeHostingAfiAccelerateConfig hitConfig = afiAccelerateConfigService.hitConfig(pledgeRatio);
		if (hitConfig == null) {
			throw new ServiceException("未命中有效AFI加速配置");
		}

		String pledgeNo = IDUtils.getSnowflakeStr();
		updateAfiWallet(userId, pledgeNo, order.getId(), afiAmountScaled.negate(), ConstantType.user_money_log_source_type.type_35, "AFI质押扣减");

		Date now = new Date();
		StakeHostingAfiPledge pledge = new StakeHostingAfiPledge();
		pledge.setPledgeNo(pledgeNo);
		pledge.setStakeHostingOrderId(order.getId());
		pledge.setStakeHostingOrderNo(order.getOrderNo());
		pledge.setUserId(order.getUserId());
		pledge.setAccount(order.getAccount());
		pledge.setStakeUsdtAmount(order.getStakeUsdtAmount());
		pledge.setAfiAmount(afiAmountScaled);
		pledge.setAfiPrice(afiPriceScaled);
		pledge.setAfiUsdtAmount(afiUsdtAmount);
		pledge.setPledgeRatio(hitConfig.getPledgeRatio());
		pledge.setAccelerateRate(hitConfig.getAccelerateRate());
		pledge.setPledgeTime(now);
		Integer effectiveDay = Integer.parseInt(DateUtil.format(DateUtil.tomorrow(), "yyyyMMdd"));
		pledge.setEffectiveDay(effectiveDay);
		pledge.setStatus(STATUS_WAIT_EFFECTIVE);
		pledge.setCreateTime(now);
		if (!save(pledge)) {
			throw new ServiceException("生成AFI质押记录失败");
		}

		boolean updateOrder = stakeHostingOrderService.lambdaUpdate()
			.eq(StakeHostingOrder::getId, order.getId())
			.eq(StakeHostingOrder::getAfiAccelerated, AFI_ACCELERATED_NO)
			.set(StakeHostingOrder::getAfiAccelerated, AFI_ACCELERATED_YES)
			.set(StakeHostingOrder::getUpdateTime, now)
			.update();
		if (!updateOrder) {
			throw new ServiceException("托管订单已绑定AFI加速");
		}
		return pledge;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int returnPledgeByOrderId(Long stakeHostingOrderId) {
		if (stakeHostingOrderId == null) {
			return 0;
		}
		StakeHostingAfiPledge pledge = lambdaQuery()
			.eq(StakeHostingAfiPledge::getStakeHostingOrderId, stakeHostingOrderId)
			.ne(StakeHostingAfiPledge::getStatus, STATUS_RETURNED)
			.one();
		if (pledge == null) {
			return 0;
		}
		String sourceCode = IDUtils.getSnowflakeStr();
		updateAfiWallet(pledge.getUserId(), sourceCode, pledge.getStakeHostingOrderId(), pledge.getAfiAmount(), ConstantType.user_money_log_source_type.type_36, "AFI质押退还");
		boolean update = lambdaUpdate()
			.eq(StakeHostingAfiPledge::getId, pledge.getId())
			.ne(StakeHostingAfiPledge::getStatus, STATUS_RETURNED)
			.set(StakeHostingAfiPledge::getStatus, STATUS_RETURNED)
			.set(StakeHostingAfiPledge::getReturnTime, new Date())
			.set(StakeHostingAfiPledge::getUpdateTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException("AFI质押记录已退还");
		}
		return 1;
	}

	private void validateOrder(StakeHostingOrder order) {
		if (order == null) {
			throw new ServiceException("托管订单不存在");
		}
		if (StakeHostingOrderServiceImpl.STATUS_RUNNING != order.getStatus()) {
			throw new ServiceException("只有产出中托管订单可以质押AFI");
		}
		if (order.getPackageDays() == null || order.getPackageDays() < 30) {
			throw new ServiceException("只有30天及以上托管套餐可以质押AFI");
		}
		if (AFI_ACCELERATED_YES == nullToZero(order.getAfiAccelerated())) {
			throw new ServiceException("托管订单已绑定AFI加速");
		}
	}

	private BigDecimal normalizeAfiPrice(BigDecimal price) {
		if (price == null) {
			throw new ServiceException("AFI价格未配置");
		}
		if (price.compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("AFI价格必须大于0");
		}
		return price.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	private void updateAfiWallet(Long userId, String sourceCode, Long sourceId, BigDecimal changeBalance, Integer sourceType, String remark) {
		UserMoneyLogVo logVo = UserMoneyLogVo.builder()
			.coinType(ConstantType.user_money_coin_type.type_2)
			.changeBalance(changeBalance)
			.sourceType(sourceType)
			.remark(remark)
			.build();
		UpdateUserMoneyVo updateUserMoneyVo = UpdateUserMoneyVo.builder()
			.userId(userId)
			.sourceCode(sourceCode)
			.sourceId(sourceId)
			.sourceType(sourceType)
			.userMoneyLogList(Collections.singletonList(logVo))
			.build();
		int update = userMoneyService.updateUserMoney(updateUserMoneyVo);
		if (update != 1) {
			throw new ServiceException("AFI钱包余额不足或更新失败");
		}
	}

	private int nullToZero(Integer value) {
		return value == null ? 0 : value;
	}
}
