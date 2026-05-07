package com.xms.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.system.SystemUtil;
import com.xms.app.entity.bo.StakeOrderBo;
import com.xms.app.entity.resp.CreateStakeHostingOrderResp;
import com.xms.app.entity.vo.CreateStakeHostingOrderVo;
import com.xms.app.service.BizStakeHostingService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.SysConstant;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.SignUtil;
import com.xms.dao.domain.StakeHostingOrder;
import com.xms.dao.domain.StakeHostingPackage;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.service.IStakeHostingOrderService;
import com.xms.dao.service.IStakeHostingPackageService;
import com.xms.dao.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.xms.app.service.impl.BizUserServiceImpl.checkWallet;

/**
 * 托管业务Service实现
 */
@Service
@Slf4j
public class BizStakeHostingServiceImpl implements BizStakeHostingService {
	private final IStakeHostingPackageService stakeHostingPackageService;
	private final IStakeHostingOrderService stakeHostingOrderService;
	private final UserInfoService userInfoService;
	private final XmsRedis xmsRedis;

	@Value("${lq.md5Key}")
	private String md5Key;

	public BizStakeHostingServiceImpl(IStakeHostingPackageService stakeHostingPackageService,
									  IStakeHostingOrderService stakeHostingOrderService,
									  UserInfoService userInfoService,
									  XmsRedis xmsRedis) {
		this.stakeHostingPackageService = stakeHostingPackageService;
		this.stakeHostingOrderService = stakeHostingOrderService;
		this.userInfoService = userInfoService;
		this.xmsRedis = xmsRedis;
	}

	@Override
	public List<StakeHostingPackage> packageList() {
		return stakeHostingPackageService.lambdaQuery()
			.eq(StakeHostingPackage::getStatus, 1)
			.orderByAsc(StakeHostingPackage::getSort)
			.orderByAsc(StakeHostingPackage::getDays)
			.list();
	}

	@Override
	public ResultPista<CreateStakeHostingOrderResp> createOrder(CreateStakeHostingOrderVo req, Long userId) {
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();
		if (userInfo == null) {
			throw new ServiceException("用户不存在");
		}
		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getAccount(), xmsRedis);
		StakeHostingOrder order = stakeHostingOrderService.createUserOrder(userId, req.getPackageId(), req.getAmount());
		CreateStakeHostingOrderResp resp = new CreateStakeHostingOrderResp();
		resp.setOrderNo(order.getOrderNo());
		resp.setStakeUsdtAmount(order.getStakeUsdtAmount());
		return ResultPista.data(resp);
	}

	@Override
	public List<StakeHostingOrder> orderList(Long lastId, Integer status) {
		List<StakeHostingOrder> list = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getUserId, SecurityUtils.getFrontUserId())
			.eq(status != null, StakeHostingOrder::getStatus, status)
			.lt(lastId != null, StakeHostingOrder::getId, lastId)
			.orderByDesc(StakeHostingOrder::getId)
			.last(SysConstant.PAGE_LIMIT)
			.list();
		if (CollectionUtil.isEmpty(list)) {
			return java.util.Collections.emptyList();
		}
		return list.stream().map(item -> {
			StakeHostingOrder order = new StakeHostingOrder();
			BeanUtil.copyProperties(item, order);
			return order;
		}).collect(Collectors.toList());
	}

	@Override
	public ResultPista<String> orderCallback(StakeOrderBo req) {
		log.info("托管订单回调 req:{}", req);
		Map<String, Object> map = BeanUtil.beanToMap(req);
		String sign = SignUtil.getSign(map, false, false, md5Key);
		String osName = SystemUtil.getOsInfo().getName();
		if (!osName.toUpperCase().contains(SysConstant.OS_NAME_WINDOWS)) {
			if (!sign.equals(req.getSign())) {
				log.error("托管订单回调验签失败");
				return ResultPista.fail("验签失败");
			}
		}
		stakeHostingOrderService.confirmChainPaid(req.getOrderNo(), req.getHash(), req.getAmount());
		return ResultPista.data("success");
	}
}
