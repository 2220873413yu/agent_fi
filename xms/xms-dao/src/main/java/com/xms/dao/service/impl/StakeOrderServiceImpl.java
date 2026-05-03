package com.xms.dao.service.impl;

import java.math.BigDecimal;
import java.util.*;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.CollectionUtil;
import com.xms.common.utils.spring.SpringUtils;
import com.xms.dao.domain.DiyStoreProduct;
import com.xms.dao.domain.UserLevelConfig;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.vo.StakeOrderProductSnapshotVo;
import com.xms.dao.service.IDiyStoreProductService;
import com.xms.dao.service.IUserLevelConfigService;
import com.xms.dao.service.UserInfoService;
import com.xms.dao.service.impl.XmsDataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xms.dao.mapper.StakeOrderMapper;
import com.xms.dao.domain.StakeOrder;
import com.xms.dao.service.IStakeOrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 质押订单Service业务层处理
 *
 * @author xms
 * @date 2026-03-09
 */
@Service
public class StakeOrderServiceImpl extends XmsDataServiceImpl<StakeOrderMapper, StakeOrder> implements IStakeOrderService
{
	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private IDiyStoreProductService diyStoreProductService;


	@Autowired
	private IUserLevelConfigService userLevelConfigService;

	@Autowired
	private AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;


	/**
     * 查询质押订单列表
     *
     *
     * @param stakeOrder 质押订单
     * @return 质押订单
     */
    @Override
    public List<StakeOrder> selectStakeOrderList(StakeOrder stakeOrder)
    {
        return baseMapper.selectStakeOrderList(stakeOrder);
    }

	@Override
	public int orderShipped(StakeOrder stakeOrder) {
		StakeOrder one = lambdaQuery()
			.eq(StakeOrder::getId, stakeOrder.getId())
			.eq(StakeOrder::getHashFirstShipOrder, 1)
			.one();
		if(one!=null){
			lambdaUpdate()
				.eq(StakeOrder::getId, stakeOrder.getId())
				.set(StakeOrder::getShipStatus, 1)
				.set(StakeOrder::getShipCompany, stakeOrder.getShipCompany())
				.set(StakeOrder::getShipNo, stakeOrder.getShipNo())
				.set(one.getShipStatus() == 0, StakeOrder::getShipTime, new Date())
				.update();
		}
		return 1;
	}

	/**
	 * 下架质押订单
	 * @param req
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int disableStakeOrder(StakeOrder req) {
		//下架
		StakeOrder stakeOrder = lambdaQuery()
			.eq(StakeOrder::getId, req.getId())
			.eq(StakeOrder::getStatus, 1)
			.one();
		if(stakeOrder == null){
			throw new ServiceException("订单状态已变更");
		}
		//减少用户业绩+团队业绩
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, stakeOrder.getUserId())
			.one();

		//查询是否有矿机
		Long userHoldCount = lambdaQuery()
			.eq(StakeOrder::getStatus, 1)
			.count();

		//个人业绩
		userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, stakeOrder.getUserId())
			.setSql("performance = performance - " + stakeOrder.getStakeUsdtAmount())
			.update();

		List<Long> parentIds = userInfo.getParentIds();
		//团队业绩
		if(CollectionUtil.isNotEmpty(parentIds)){
			boolean update = userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, parentIds)
				.setSql("umbrella_performance = umbrella_performance - " + stakeOrder.getStakeUsdtAmount())
				.update();
			if(!update){
				throw new ServiceException("更新团队质押业绩失败");
			}
		}

		parentIds.addLast(userInfo.getUserId());
		List<UserInfo> parentUserInfoList = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, parentIds)
			.orderByDesc(UserInfo::getUserId)
			.list();

		List<UserLevelConfig> userLevelConfigList = userLevelConfigService.lambdaQuery()
			.gt(UserLevelConfig::getLevel,0)
			.orderByAsc(UserLevelConfig::getLevel)
			.list();
		for (UserInfo info : parentUserInfoList) {
			SpringUtils.getBean(StakeOrderServiceImpl.class).callUserLevel(info, userLevelConfigList);
		}

		boolean update = lambdaUpdate()
			.eq(StakeOrder::getId, req.getId())
			.eq(StakeOrder::getStatus, 1)
			.set(StakeOrder::getStatus, 6)
			.set(StakeOrder::getUpdateTime, new Date())
			.update();
		if(!update){
			throw new ServiceException("订单状态已变更");
		}
		return 1;
	}


	/**
	 * 计算用户等级
	 * @param info
	 * @param userLevelConfigList
	 */
	@Override
	public void callUserLevel(UserInfo info, List<UserLevelConfig> userLevelConfigList) {
	/*
		Integer origLevel = info.getGameLevel();
		Integer currentLevel = 0;
		if(info.getPerformance().compareTo(BigDecimal.ZERO)>0){
			// 计算直推线数量
			List<UserInfo> directUserList = userInfoService.lambdaQuery()
				.eq(UserInfo::getInviteUserId, info.getUserId())
				.select(UserInfo::getUserId,UserInfo::getGameLevel,UserInfo::getPerformance,
					UserInfo::getMaxLegPerformance,UserInfo::getUmbrellaPerformance)
				.list();
			Map<Long, List<UserInfo>> childUserListMap = new HashMap<>();
			for (UserLevelConfig userLevelConfig : userLevelConfigList) {
				// 大区业绩+小区业绩+自身业绩满足档位条件
				//计算大区业绩
				if(info.getMaxLegPerformance().compareTo(userLevelConfig.getTeamPerformance())>=0
					//小区业绩
					&& info.getCommunityPerformance().compareTo(userLevelConfig.getCommunityPerformance())>=0
					//自身业绩大于1
					&& info.getPerformance().compareTo(userLevelConfig.getPerformance())>=0){
					if(userLevelConfig.getLevel()>1){
						if(cn.hutool.core.collection.CollectionUtil.isEmpty(directUserList) || directUserList.size()<userLevelConfig.getRequiredLegNum()){
							continue;
						}

						Integer legLevelMin = userLevelConfig.getLegLevelMin();
						Integer legLevelCount = userLevelConfig.getLegLevelCount();
						int hitLines = 0;
						// 逐条线统计是否满足“该等级人数”要求
						for (UserInfo childUser : directUserList) {
							List<UserInfo> childUserList = childUserListMap.computeIfAbsent(childUser.getUserId(), userId -> {
								List<UserInfo> list = userInfoService.getChildUserList(userId);
								if (list == null) {
									list = new ArrayList<>();
								}
								// 该线包含直推本人
								list.add(childUser);
								return list;
							});
							long matchCount = childUserList.stream()
								.filter(u -> u.getGameLevel() != null && legLevelMin != null && u.getGameLevel() >= legLevelMin)
								.count();
							// 该线满足人数要求则计为命中
							if (legLevelCount == null || matchCount >= legLevelCount) {
								hitLines++;
							}
							// 命中线数已满足要求，提前结束
							if (hitLines >= userLevelConfig.getRequiredLegNum()) {
								break;
							}
						}
						// 命中线数不足，跳过该档位
						if (hitLines < userLevelConfig.getRequiredLegNum()) {
							continue;
						}
					}
					// 记录当前满足的最高档位
					currentLevel = userLevelConfig.getLevel();
				}
			}
		}

		if(currentLevel !=origLevel){
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, info.getUserId())
				.set(UserInfo::getGameLevel, currentLevel)
				.update();
		}*/
	}

	@Override
	public int updateStakeOrderById(StakeOrder req) {
		if(StrUtil.isBlank(req.getPayHash())){
			throw new ServiceException("支付交易哈希不能为空");
		}
		Long count = lambdaQuery()
			.eq(StakeOrder::getPayHash, req.getPayHash())
			.count();
		if(count>0){
			throw new ServiceException("支付交易哈希已存在");
		}

		StakeOrder queryStakeOrder = lambdaQuery()
			.eq(StakeOrder::getId, req.getId())
			.eq(StakeOrder::getStatus, 0)
			.one();
		if (queryStakeOrder == null) {
			throw new ServiceException("订单状态已修改");
		}
		SpringUtils.getBean(StakeOrderServiceImpl.class).doStakeOrderCallback(queryStakeOrder,queryStakeOrder.getUserId());
		return 1;
	}

	/**
	 * 购买矿机回调
	 * @param queryStakeOrder
	 * @param userId
	 */
	@RedisLock(value = RedisConstant.LockConstant.XMS_BUY_MINING_CALL_BACK, param = "#userId")
	@Transactional(rollbackFor = Exception.class)
	public void doStakeOrderCallback(StakeOrder queryStakeOrder,Long userId) {
		//查询用户
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();

		StakeOrderProductSnapshotVo stakeOrderProductSnapshotVo = JSON.parseObject(queryStakeOrder.getProductSnapshot(), StakeOrderProductSnapshotVo.class);
		//更新商品销量
		diyStoreProductService.lambdaUpdate()
			.eq(DiyStoreProduct::getId, Long.valueOf(stakeOrderProductSnapshotVo.getProductId()))
			.setSql("sales = sales + " + queryStakeOrder.getNum())
			.update();
		boolean update = lambdaUpdate()
			.eq(StakeOrder::getId, queryStakeOrder.getId())
			.eq(StakeOrder::getStatus, queryStakeOrder.getStatus())
			.set(StakeOrder::getStatus, 1)
			.set(StakeOrder::getPayTime, new Date())
			.set(StakeOrder::getPayHash, queryStakeOrder.getPayHash())
			.set(StakeOrder::getUpdateTime, new Date())
			.update();
		if (!update) {
			throw new ServiceException("更新质押订单失败");
		}

		update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userInfo.getUserId())
			.setSql("performance = performance + " + queryStakeOrder.getStakeUsdtAmount())
			.update();
		if (!update) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		List<Long> parentIds = userInfo.getParentIds();
		if(CollectionUtil.isNotEmpty(parentIds)){
			update = userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, parentIds)
				.setSql("umbrella_performance = umbrella_performance + " + queryStakeOrder.getStakeUsdtAmount())
				.update();
			if (!update) {
				throw new ServiceException(ResponseCode.CODE_1002);
			}

			//计算小区业绩和大区业绩
			calculateCommunityPerformance(parentIds);
		}

		//后续费的分红信息、和等级计算
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
				OrderMsgDO orderMsgDO = new OrderMsgDO();
				orderMsgDO.setId(queryStakeOrder.getId());
				orderMsgDO.setBizType(1);
				orderMsgDOList.add(orderMsgDO);
				asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
			}
		});
	}

	/**
	 * 管理后台和app计算都有计算等级的地方
	 * 计算小区业绩+大区业绩
	 *
	 * @param parentIds
	 */
	@Override
	public void calculateCommunityPerformance(List<Long> parentIds) {
		// 小区业绩（对所有上级计算：去掉最大直推线）
		if (CollectionUtil.isNotEmpty(parentIds)) {
			for (Long parentId : parentIds) {
				List<UserInfo> children = userInfoService.lambdaQuery()
					.eq(UserInfo::getInviteUserId, parentId)
					.select(UserInfo::getUserId, UserInfo::getUmbrellaPerformance,
						UserInfo::getPerformance)
					.list();
				BigDecimal maxLegPerformance = UserInfoServiceImpl.getMaxTeamPerformance(children);
				if (CollectionUtil.isEmpty(children) || children.size() <= 1) {
					//更新小区业绩、和大区业绩
					userInfoService.lambdaUpdate()
						.eq(UserInfo::getUserId, parentId)
						.set(UserInfo::getCommunityPerformance, BigDecimal.ZERO)
						.update();
					continue;
				}
				BigDecimal totalChildPerformance = BigDecimal.ZERO;
				BigDecimal maxChildPerformance = BigDecimal.ZERO;
				for (UserInfo child : children) {
					BigDecimal childUmbrella = child.getUmbrellaPerformance();
					BigDecimal performance = child.getPerformance();
					childUmbrella = childUmbrella.add(performance);

					totalChildPerformance = totalChildPerformance.add(childUmbrella);
					if (childUmbrella.compareTo(maxChildPerformance) > 0) {
						maxChildPerformance = childUmbrella;
					}
				}
				BigDecimal communityPerformance = totalChildPerformance.subtract(maxChildPerformance);
				if (communityPerformance.compareTo(BigDecimal.ZERO) < 0) {
					communityPerformance = BigDecimal.ZERO;
				}

				//更新小区业绩和大区业绩
				userInfoService.lambdaUpdate()
					.eq(UserInfo::getUserId, parentId)
					.set(UserInfo::getCommunityPerformance, communityPerformance)
					.update();
			}
		}
	}
}
