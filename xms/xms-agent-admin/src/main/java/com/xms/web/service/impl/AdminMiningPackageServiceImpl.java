package com.xms.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.MiningPackage;
import com.xms.dao.domain.MiningPackageOrder;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.req.AdminAllocateMiningMachineRequest;
import com.xms.dao.service.IMiningPackageOrderService;
import com.xms.dao.service.IMiningPackageService;
import com.xms.dao.service.UserInfoService;
import com.xms.web.service.AdminMiningPackageService;
import com.xms.web.service.BizCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminMiningPackageServiceImpl implements AdminMiningPackageService {
	@Autowired
	private AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private IMiningPackageOrderService miningPackageOrderService;

	@Autowired
	private IMiningPackageService miningPackageService;

	@Autowired
	private BizCommonService bizCommonService;

//	@Override
//	@Transactional(rollbackFor = Exception.class)
//	@RedisLock(value = RedisConstant.LockConstant.XMS_BUY_MINING_APPLY, param = "#userId")
//	public int adminAllocateMiningMachine(AdminAllocateMiningMachineRequest req, Long userId,
//										  UserInfo userInfo) {
//		if(req.getAccount() == null || StrUtil.isBlank(req.getAccount())){
//			throw new ServiceException("请选择用户");
//		}
//
//		MiningPackage miningPackage = miningPackageService.lambdaQuery()
//			.last("limit 1")
//			.one();
//		if(miningPackage.getAvailableStock()<1){
//			throw new ServiceException("库存不足");
//		}
//		boolean update = miningPackageService.lambdaUpdate()
//			.eq(MiningPackage::getId, miningPackage.getId())
//			.gt(MiningPackage::getAvailableStock, 0)
//			.setSql("available_stock = available_stock - 1 ")
//			.setSql("sales = sales + 1 ")
//			.update();
//		if(!update){
//			throw new ServiceException("更新库存失败");
//		}
//		BigDecimal unitPrice = miningPackage.getPrice();
//		//主订单号
//		String masterOrderNo = IDUtils.getSnowflake().nextIdStr();
//		//订单号
//		String orderNo = IDUtils.getSnowflake().nextIdStr();
//		//插入订单
//		MiningPackageOrder packageOrder = new MiningPackageOrder();
//		packageOrder.setPayType(req.getPayType());
//		packageOrder.setSourceType(0);
//		packageOrder.setMasterOrderNo(masterOrderNo);
//		packageOrder.setOrderValueUsdt(unitPrice);
//		packageOrder.setPayValidNum2(BigDecimal.ZERO);
//		packageOrder.setPayValidNum1(BigDecimal.ZERO);
//		packageOrder.setPayValidNum4(BigDecimal.ZERO);
//		if (req.getPayType().equals(1)) {
//			packageOrder.setPayValidNum1(unitPrice);
//			packageOrder.setDfcPrice(BigDecimal.ZERO);
//		} else {
//			//算出要支付多少的dfc
//			//获取dfc价格
//			BigDecimal dFcPrice = bizCommonService.getDFcPrice();
//			BigDecimal payDfcAmount = unitPrice
//				.divide(dFcPrice, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
//			packageOrder.setPayValidNum2(payDfcAmount);
//			packageOrder.setDfcPrice(dFcPrice);
//		}
//
//		packageOrder.setOrderNo(orderNo);
//		packageOrder.setUserId(userId);
//		packageOrder.setRunDays(0);
//		packageOrder.setSourceType(1);
//		packageOrder.setComputingPower(new BigDecimal(miningPackage.getRemark()));
//		packageOrder.setDayReward(BigDecimal.ZERO);
//		packageOrder.setTotalReward(BigDecimal.ZERO);
//		packageOrder.setStatus(0);
//		packageOrder.setBizStatus(0);
//		packageOrder.setBizStatus1(0);
//		packageOrder.setCreateTime(new Date());
//
//		boolean save = miningPackageOrderService.save(packageOrder);
//		if(!save){
//			throw new ServiceException("添加订单失败");
//		}
//
//		if(userInfo.getIsValid() == 0){
//			userInfoService.lambdaUpdate()
//				.eq(UserInfo::getUserId, userInfo.getUserId())
//				.set(UserInfo::getIsValid,1)
//				.update();
//		}
//
//		//添加自身业绩
//		update = userInfoService.lambdaUpdate()
//			.eq(UserInfo::getUserId, userId)
//			.setSql("performance = performance + 1")
//			.update();
//		if(!update){
//			throw new ServiceException(ResponseCode.CODE_1002);
//		}
//
//		//直推、团队、小区业绩
//		List<Long> parentIds = userInfo.getParentIds();
//		if(CollectionUtil.isNotEmpty(parentIds)){
//			update = userInfoService.lambdaUpdate()
//				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
//				.setSql("sub_performance = sub_performance + 1 ")
//				.update();
//			if(!update){
//				throw new ServiceException(ResponseCode.CODE_1002);
//			}
//
//			update = userInfoService.lambdaUpdate()
//				.in(UserInfo::getUserId, parentIds)
//				.setSql("umbrella_performance = umbrella_performance + 1 ")
//				.update();
//			if(!update){
//				throw new ServiceException(ResponseCode.CODE_1002);
//			}
//
//			//计算小区业绩
//			calculateCommunityPerformance(parentIds);
//		}
//
//		//后续费的分红信息、和等级计算
//		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//			@Override
//			public void afterCommit() {
//				List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
//				OrderMsgDO orderMsgDO = new OrderMsgDO();
//				orderMsgDO.setId(packageOrder.getId());
//				orderMsgDO.setBizType(1);
//				orderMsgDOList.add(orderMsgDO);
//				asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
//			}
//		});
//		return 1;
//	}
//
//	/**
//	 * 计算小区业绩
//	 * @param parentIds
//	 */
//	private void calculateCommunityPerformance(List<Long> parentIds) {
//		// 小区业绩（对所有上级计算：去掉最大直推线）
//		Map<Long, BigDecimal> userCommunityPerformanceMap = userInfoService.lambdaQuery()
//			.in(UserInfo::getUserId, parentIds)
//			.select(UserInfo::getUserId,UserInfo::getCommunityPerformance)
//			.list().stream().collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getCommunityPerformance, (k1, k2) -> k2));
//		if (CollectionUtil.isNotEmpty(parentIds)) {
//			for (Long parentId : parentIds) {
//				List<UserInfo> children = userInfoService.lambdaQuery()
//					.eq(UserInfo::getInviteUserId, parentId)
//					.select(UserInfo::getUserId,UserInfo::getUmbrellaPerformance,
//						UserInfo::getPerformance)
//					.list();
//				if (CollectionUtil.isEmpty(children) || children.size() <= 1) {
//					if(userCommunityPerformanceMap.get(parentId).compareTo(BigDecimal.ZERO)!=0){
//						userInfoService.lambdaUpdate()
//							.eq(UserInfo::getUserId, parentId)
//							.set(UserInfo::getCommunityPerformance, BigDecimal.ZERO)
//							.update();
//					}
//					continue;
//				}
//				BigDecimal totalChildPerformance = BigDecimal.ZERO;
//				BigDecimal maxChildPerformance = BigDecimal.ZERO;
//				for (UserInfo child : children) {
//					BigDecimal childUmbrella = child.getUmbrellaPerformance();
//					BigDecimal performance = child.getPerformance();
//					childUmbrella = childUmbrella.add(performance);
//
//					totalChildPerformance = totalChildPerformance.add(childUmbrella);
//					if (childUmbrella.compareTo(maxChildPerformance) > 0) {
//						maxChildPerformance = childUmbrella;
//					}
//				}
//				BigDecimal communityPerformance = totalChildPerformance.subtract(maxChildPerformance);
//				if (communityPerformance.compareTo(BigDecimal.ZERO) < 0) {
//					communityPerformance = BigDecimal.ZERO;
//				}
//
//				if(userCommunityPerformanceMap.get(parentId).compareTo(communityPerformance)!=0){
//					userInfoService.lambdaUpdate()
//						.eq(UserInfo::getUserId, parentId)
//						.set(UserInfo::getCommunityPerformance, communityPerformance)
//						.update();
//				}
//			}
//		}
//	}

}
