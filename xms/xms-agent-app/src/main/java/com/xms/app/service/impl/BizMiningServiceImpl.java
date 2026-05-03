package com.xms.app.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import com.alibaba.fastjson.JSON;
import com.xms.app.entity.dto.*;
import com.xms.app.entity.req.NodePackageReq;
import com.xms.app.entity.req.SwapOrderCallbackReq;
import com.xms.app.service.BizCommonService;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.app.entity.resp.CreateOrderResp;
import com.xms.common.constant.ConstantType;
import com.xms.app.entity.vo.*;
import com.xms.common.mq.dynamic.AsyncDynamicOrderSettlementService;
import com.xms.app.service.BizMiningService;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.config.redis.delayqueue.RedissonDelayHandler;
import com.xms.common.config.redis.delayqueue.RedissonDelayOrder;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.*;
import com.xms.common.core.domain.api.ResultPista;
import com.xms.common.exception.ServiceException;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.Func;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.SignUtil;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.*;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.domain.UserMoneyLog;
import com.xms.dao.entity.vo.ParentUserTaskVo;
import com.xms.dao.service.*;
import com.xms.dao.service.impl.MiningPackageOrderServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.xms.app.service.impl.BizUserServiceImpl.checkWallet;

@Service
@Slf4j
public class BizMiningServiceImpl implements BizMiningService {
	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private ISysParaService sysParaServiceImpl;
	@Autowired
	private AsyncDynamicOrderSettlementService asyncDynamicOrderSettlementServiceImpl;

	@Autowired
	private RedissonDelayHandler redissonDelayHandler;

	@Value("${lq.md5Key}")
	private String md5Key;

	@Value("${lq.baseUrl}")
	private String baseUrl;

	@Autowired
	private ISwapOrderService iSwapOrderService;

	@Autowired
	private IMiningPackageService miningPackageService;

	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private IUserMoneyService userMoneyService;

	@Autowired
	private UserWalletService userWalletService;

	@Autowired
	private IMiningPackageOrderService miningPackageOrderService;

	@Autowired
	private IRewardRecordService rewardRecordService;

	@Autowired
	private BizCommonService bizCommonService;

	@Autowired
	private ISysParaService sysParaService;

	@Autowired
	private IUserAddressService userAddressServiceImpl;

	@Autowired
	private IMiningPackageTierService miningPackageTierService;


	@Override
	public ResultPista<MiningPackageTierDto> getPackageTierById(Long id) {
		List<MiningPackageTier> tierList = miningPackageTierService.lambdaQuery()
			.list();
		MiningPackageTier miningPackageTier = matchMiningPackageTier(tierList, id.intValue());
		if(miningPackageTier != null){
			MiningPackageTierDto resp = new MiningPackageTierDto();
			resp.setStakeAmount(miningPackageTier.getStakeAmount());
			resp.setDayReward(miningPackageTier.getDayReward());
			return ResultPista.data(resp);
		}
		return ResultPista.data(new MiningPackageTierDto());
	}


	/**
	 * 我的矿机列表
	 * @param lastId id
	 * @param bizType 0:未质押,1:已质押,2:自提订单
	 * @return
	 */
	@Override
	public List<MyMiningListDto> myMiningList(Long lastId, Integer bizType) {
		if(bizType == null){
			return new ArrayList<>();
		}
		List<MyMiningListDto> miningListDtos = miningPackageOrderService.lambdaQuery()
			.eq(MiningPackageOrder::getUserId, SecurityUtils.getFrontUserId())
			.lt(Func.isNotEmpty(lastId), MiningPackageOrder::getId, lastId)
			.eq(bizType == 0, MiningPackageOrder::getStatus, 0)
			.in(bizType == 1, MiningPackageOrder::getStatus, 1, 2)
			.eq(bizType == 2, MiningPackageOrder::getStakeType, 2)
			.orderByDesc(MiningPackageOrder::getId).last(SysConstant.PAGE_LIMIT)
			.list()
			.stream().map(item -> {
				MyMiningListDto dto = new MyMiningListDto();
				BeanUtil.copyProperties(item, dto);
				if(dto.getTotalReward().compareTo(BigDecimal.ZERO)>0){
					dto.setTodayOutDfc(item.getDayReward());
				}else{
					dto.setTodayOutDfc(BigDecimal.ZERO);
				}
				//设置矿机标号
				dto.setMiningNo(item.getId().toString());
				return dto;
			}).collect(Collectors.toList());
		return miningListDtos;
	}

	@Override
	public MyMiningInfoDto myMiningInfo() {
		MyMiningInfoDto result = new MyMiningInfoDto();
		List<MiningPackageOrder> packageOrderList = miningPackageOrderService.lambdaQuery()
			.eq(MiningPackageOrder::getUserId, SecurityUtils.getLoginAppUser().getUserId())
			.list();
		if(CollectionUtil.isNotEmpty(packageOrderList)){
			//对packageOrderList中computingPower求和
			BigDecimal computingPower = packageOrderList.stream().map(MiningPackageOrder::getComputingPower).reduce(BigDecimal.ZERO, BigDecimal::add);
			result.setMiningComputingPower(computingPower);
			result.setMiningNum(packageOrderList.size());
			//想知道packageOrderList中status不是0的记录有多少条
			result.setStakeMiningNum(packageOrderList.stream().filter(item -> item.getStatus() != 0).count());
			//想知道packageOrderList中status不是0的记录的computingPower求和
			BigDecimal stakeMiningComputingPower = packageOrderList.stream().filter(item -> item.getStatus() != 0).map(MiningPackageOrder::getComputingPower).reduce(BigDecimal.ZERO, BigDecimal::add);
			result.setStakeMiningComputingPower(stakeMiningComputingPower);
			BigDecimal todayOutDfc= BigDecimal.ZERO;
			for (MiningPackageOrder order : packageOrderList) {
				if(order.getTotalReward().compareTo(BigDecimal.ZERO)>0){
					todayOutDfc = todayOutDfc.add(order.getTotalReward());
				}
			}
			result.setTodayOutDfc(todayOutDfc);
		}
		return result;
	}

	/**
	 * 质押矿机订单
	 *
	 * @param req
	 * @param userId
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	//@RedisLock(value = RedisConstant.LockConstant.XMS_TRANSFER_APPLY, param = "#userId")
	@RedisLock(value = RedisConstant.LockConstant.XMS_BUY_MINING_APPLY, param = "#userId")
	public ResultPista<CreateOrderResp> stakeMiningOrder(StakeMiningOrderVo req, Long userId) {
		String userAddressJsonStr = "";
		if(req.getType() == 2){
			if(req.getAddressId() == null){
				throw new ServiceException(ResponseCode.CODE_1002);
			}
			UserAddress userAddress = userAddressServiceImpl.lambdaQuery()
				.eq(UserAddress::getId, req.getAddressId())
				.eq(UserAddress::getUserId, SecurityUtils.getFrontUserId())
				.one();
			if(userAddress == null){
				throw new ServiceException(ResponseCode.CODE_1002);
			}
			userAddressJsonStr = JSON.toJSONString(userAddress);
		}
		// 校验订单归属且未质押
		MiningPackageOrder queryOrder = getPendingStakeOrder(req.getId(), userId);
		queryOrder.setMiningNo(queryOrder.getId().toString());
		// 匹配矿机编号所在档位
		MiningPackageTier matchedTier = resolveStakeTier(queryOrder.getMiningNo());
		// 扣除质押金额
		debitStakeDfc(userId, matchedTier.getStakeAmount(), queryOrder.getOrderNo());
		// 更新质押状态与档位信息
		updateStakeOrderStatus(req.getId(), userId, matchedTier,req.getType(),userAddressJsonStr);
		// 发放直推与间推奖励
		grantStakeRewards(userId, matchedTier, queryOrder.getOrderNo());

		return ResultPista.success();
	}

	// 校验订单归属与未质押状态
	private MiningPackageOrder getPendingStakeOrder(Long orderId, Long userId) {
		MiningPackageOrder queryOrder = miningPackageOrderService.lambdaQuery()
			.eq(MiningPackageOrder::getId, orderId)
			.eq(MiningPackageOrder::getUserId, userId)
			.eq(MiningPackageOrder::getStatus, 0)
			.one();
		if (queryOrder == null) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		return queryOrder;
	}

	// 依据矿机编号匹配质押档位
	private MiningPackageTier resolveStakeTier(String miningNo) {
		Integer miningNoValue = Integer.valueOf(miningNo);
		List<MiningPackageTier> list = miningPackageTierService.lambdaQuery()
			.orderByAsc(MiningPackageTier::getStartIndex)
			.list();
		return matchMiningPackageTier(list, miningNoValue);
	}

	// 扣除质押DFC金额
	private void debitStakeDfc(Long userId, BigDecimal stakeAmount, String orderNo) {
		UserMoney userMoney = userMoneyService.lambdaQuery()
			.eq(UserMoney::getId, userId)
			.one();
		if (userMoney.getValidNum2().compareTo(stakeAmount) < 0) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
		int count = userWalletService.handerUserMoney(stakeAmount.negate(), orderNo,
			userId, userId, ConstantType.user_money_log_source_type.type_13, ConstantType.user_money_coin_type.type_2);
		if (count != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
	}

	/**
	 * 更新订单状态与档位参数
	 * @param orderId
	 * @param userId
	 * @param matchedTier
	 * @param stakeType 质押类型 1:托管,2:自提
	 */
	private void updateStakeOrderStatus(Long orderId, Long userId, MiningPackageTier matchedTier,
										Integer stakeType,String userAddressJsonStr) {
		Integer stakeDays = Integer.valueOf(sysParaService.getValue(ConstantSys.biz_stake_startup_days));
		if (stakeDays > 0) {
			// 启动中
			boolean update = miningPackageOrderService.lambdaUpdate()
				.eq(MiningPackageOrder::getId, orderId)
				.eq(MiningPackageOrder::getUserId, userId)
				.eq(MiningPackageOrder::getStatus, 0)
				.set(MiningPackageOrder::getStatus, 1)
				.set(MiningPackageOrder::getStakeDfcAmount, matchedTier.getStakeAmount())
				.set(MiningPackageOrder::getDayReward, matchedTier.getDayReward())
				.set(MiningPackageOrder::getStakeStartupRemainingDays, stakeDays)
				.set(MiningPackageOrder::getUpdateTime, new Date())
				.set(MiningPackageOrder::getStakeDate, new Date())
				.set(MiningPackageOrder::getStakeType, stakeType)
				.set(MiningPackageOrder::getBizStatus1, 1)
				.set(stakeType == 2, MiningPackageOrder::getRemark, userAddressJsonStr)
				.update();
			if (!update) {
				throw new ServiceException(ResponseCode.CODE_1002);
			}
		} else {
			// 直接启动
			boolean update = miningPackageOrderService.lambdaUpdate()
				.eq(MiningPackageOrder::getId, orderId)
				.eq(MiningPackageOrder::getUserId, userId)
				.eq(MiningPackageOrder::getStatus, 0)
				.set(MiningPackageOrder::getStakeDfcAmount, matchedTier.getStakeAmount())
				.set(MiningPackageOrder::getDayReward, matchedTier.getDayReward())
				.set(MiningPackageOrder::getStatus, 2)
				.set(MiningPackageOrder::getStakeStartupRemainingDays, 0)
				.set(MiningPackageOrder::getUpdateTime, new Date())
				.update();
			if (!update) {
				throw new ServiceException(ResponseCode.CODE_1002);
			}
		}
	}

	private void grantInviteReward(Long rewardUserId, BigDecimal rewardRatio, BigDecimal orderReward,
								   String orderNo, Long sourceUserId, Integer rewardCoinType,
								   Integer userMoneySourceType, Integer rewardRecordSourceType) {
		if(rewardUserId == null || rewardRatio == null){
			return;
		}
		BigDecimal rewardAmount = rewardRatio.multiply(orderReward)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if(rewardAmount.compareTo(BigDecimal.ZERO) <= 0){
			return;
		}
		int count = userWalletService.handerUserMoney(rewardAmount, orderNo,
			rewardUserId, sourceUserId, userMoneySourceType, rewardCoinType);
		if (count != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		RewardRecord rewardRecordEntity = new RewardRecord();
		rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
		rewardRecordEntity.setUserId(rewardUserId);
		rewardRecordEntity.setAmount(rewardAmount);
		rewardRecordEntity.setSourceType(rewardRecordSourceType);
		rewardRecordEntity.setCoinType(rewardCoinType);
		rewardRecordEntity.setSourceUserId(sourceUserId);
		rewardRecordEntity.setSourceOrderCode(orderNo);
		rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
		rewardRecordService.save(rewardRecordEntity);
	}

	// 发放直推与间推奖励（仅有效用户）
	private void grantStakeRewards(Long userId, MiningPackageTier matchedTier, String orderNo) {
		// 质押奖励比例仍沿用原配置，只是奖励对象改为邀请链上最近两个有效上级
		BigDecimal inviteRatio = new BigDecimal(sysParaService.getValue(ConstantSys.biz_stake_invite_ratio))
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		BigDecimal indirectRatio = new BigDecimal(sysParaService.getValue(ConstantSys.biz_stake_indirect_ratio))
			.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.select(UserInfo::getUserId, UserInfo::getInviteUserId)
			.one();
		if (userInfo.getInviteUserId() != null) {
			// 按邀请链向上查找最近两个有效上级：第一个有效用户发直推，第二个有效用户发间推
			List<ParentUserTaskVo> validParentUsers = userInfoService.getValidParentUserTaskVo(userInfo.getUserId(), 2);
			if(CollectionUtil.isNotEmpty(validParentUsers)){
				ParentUserTaskVo directUser = validParentUsers.get(0);
				// 最近的有效上级作为直推奖励接收人
				grantInviteReward(directUser.getUserId(), inviteRatio, matchedTier.getStakeAmount(), orderNo, userInfo.getUserId(),
					ConstantType.user_money_coin_type.type_2, ConstantType.user_money_log_source_type.type_14,
					ConstantType.xms_reward_record_source_type.type_15);

				if(validParentUsers.size() > 1){
					ParentUserTaskVo indirectUser = validParentUsers.get(1);
					// 在直推上级之上继续找到的下一个有效用户作为间推奖励接收人
					grantInviteReward(indirectUser.getUserId(), indirectRatio, matchedTier.getStakeAmount(), orderNo, userInfo.getUserId(),
						ConstantType.user_money_coin_type.type_2, ConstantType.user_money_log_source_type.type_15,
						ConstantType.xms_reward_record_source_type.type_16);
				}
			}
		}
		/*if (userInfo.getInviteUserId() != null) {
			UserInfo inviteUserInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.select(UserInfo::getUserId, UserInfo::getInviteUserId, UserInfo::getIsValid)
				.one();
			if (inviteUserInfo != null && inviteUserInfo.getIsValid() == 1) {
				// 直推奖励
				BigDecimal inviteRatio = new BigDecimal(sysParaService.getValue(ConstantSys.biz_stake_invite_ratio))
					.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
				BigDecimal inviteReward = matchedTier.getStakeAmount().multiply(inviteRatio)
					.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
				if (inviteReward.compareTo(BigDecimal.ZERO) > 0) {
					int count = userWalletService.handerUserMoney(inviteReward, orderNo,
						inviteUserInfo.getUserId(), userId, ConstantType.user_money_log_source_type.type_14,
						ConstantType.user_money_coin_type.type_2);
					if (count != 1) {
						throw new ServiceException(ResponseCode.CODE_1015);
					}

					RewardRecord rewardRecordEntity = new RewardRecord();
					rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
					rewardRecordEntity.setUserId(inviteUserInfo.getUserId());
					rewardRecordEntity.setAmount(inviteReward);
					rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_15);
					rewardRecordEntity.setCoinType(ConstantType.user_money_coin_type.type_2);
					rewardRecordEntity.setSourceUserId(userInfo.getUserId());
					rewardRecordEntity.setSourceOrderCode(orderNo);
					rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
					rewardRecordService.save(rewardRecordEntity);
				}
			}
			if (inviteUserInfo != null) {
				UserInfo indirectUserInfo = userInfoService.lambdaQuery()
					.eq(UserInfo::getUserId, inviteUserInfo.getInviteUserId())
					.select(UserInfo::getUserId, UserInfo::getInviteUserId, UserInfo::getIsValid)
					.one();
				if (indirectUserInfo != null && indirectUserInfo.getIsValid() == 1) {
					// 间推奖励
					BigDecimal indirectRatio = new BigDecimal(sysParaService.getValue(ConstantSys.biz_stake_indirect_ratio))
						.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
					BigDecimal indirectReward = matchedTier.getStakeAmount().multiply(indirectRatio)
						.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
					if (indirectReward.compareTo(BigDecimal.ZERO) > 0) {
						int count = userWalletService.handerUserMoney(indirectReward, orderNo,
							indirectUserInfo.getUserId(), userId, ConstantType.user_money_log_source_type.type_15,
							ConstantType.user_money_coin_type.type_2);
						if (count != 1) {
							throw new ServiceException(ResponseCode.CODE_1015);
						}

						RewardRecord rewardRecordEntity = new RewardRecord();
						rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
						rewardRecordEntity.setUserId(indirectUserInfo.getUserId());
						rewardRecordEntity.setAmount(indirectReward);
						rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_16);
						rewardRecordEntity.setCoinType(ConstantType.user_money_coin_type.type_2);
						rewardRecordEntity.setSourceUserId(userInfo.getUserId());
						rewardRecordEntity.setSourceOrderCode(orderNo);
						rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
						rewardRecordService.save(rewardRecordEntity);
					}
				}
			}
		}*/
	}

	private MiningPackageTier matchMiningPackageTier(List<MiningPackageTier> list, Integer miningNo) {
		if (list == null || list.isEmpty()) {
			throw new ServiceException("质押档位未配置");
		}
		for (MiningPackageTier tier : list) {
			Integer startIndex = tier.getStartIndex();
			Integer endIndex = tier.getEndIndex();
			if (startIndex == null || endIndex == null) {
				continue;
			}
			if (miningNo >= startIndex && miningNo <= endIndex) {
				return tier;
			}
		}
		return list.get(list.size() - 1);
	}

/*
	@Override
	@Transactional(rollbackFor = Exception.class)
	@RedisLock(value = RedisConstant.LockConstant.XMS_BUY_MINING_APPLY, param = "#userId")
	public ResultPista<CreateOrderResp> miningOrder(CreateMiningOrderVo req, Long userId) {
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, userId)
			.one();

		//验签，随机数
		checkWallet(req.getRandomNum(), req.getSignature(), userInfo.getAccount(), xmsRedis);

		//套餐是否上架
		MiningPackage miningPackage = miningPackageService.lambdaQuery()
			.eq(MiningPackage::getId, req.getId())
			.eq(MiningPackage::getStatus, 1)
			.one();
		if (miningPackage == null) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		//检查库存
		if(miningPackage.getAvailableStock()<req.getNum()){
			throw new ServiceException(ResponseCode.CODE_1259);
		}

		//扣减库存
		boolean update = miningPackageService.lambdaUpdate()
			.eq(MiningPackage::getId, miningPackage.getId())
			.ge(MiningPackage::getAvailableStock, req.getNum())
			.setSql("available_stock = available_stock - " + req.getNum())
			.setSql("sales = sales + " + req.getNum())
			.update();
		if (!update) {
			throw new ServiceException(ResponseCode.CODE_1259);
		}

		// 主单号：一次性扣款与流水归集用
		String masterOrderNo = IDUtils.getSnowflake().nextIdStr();
		BigDecimal unitPrice = miningPackage.getPrice();
		BigDecimal totalPrice = unitPrice.multiply(new BigDecimal(req.getNum()));

		// 余额校验 + 一次性扣款，流水来源使用主单号
		UserMoney userInfoMoney = userMoneyService.lambdaQuery()
			.eq(UserMoney::getId, userId)
			.one();
		PaySummary paySummary = handleMiningPackagePay(req.getPayType(), totalPrice, userInfoMoney, masterOrderNo, userId);

		// 按订单分摊扣款明细，便于后续对账（资金已一次性扣完）
		List<MiningPackageOrder> orderList = new ArrayList<>(req.getNum());
		BigDecimal remainingValidNum4 = paySummary.getPayValidNum4();
		BigDecimal remainingValidNum1 = paySummary.getPayValidNum1();
		BigDecimal remainingValidNum2 = paySummary.getPayValidNum2();
		for (Integer i = 0; i < req.getNum(); i++) {
			//订单号
			String orderNo = IDUtils.getSnowflake().nextIdStr();
			//生成订单
			MiningPackageOrder packageOrder = new MiningPackageOrder();
			packageOrder.setPayType(req.getPayType());
			packageOrder.setMasterOrderNo(masterOrderNo);
			packageOrder.setSourceType(0);
			packageOrder.setComputingPower(new BigDecimal(miningPackage.getRemark()));
			packageOrder.setOrderValueUsdt(unitPrice);
			packageOrder.setPayValidNum2(BigDecimal.ZERO);
			packageOrder.setPayValidNum1(BigDecimal.ZERO);
			packageOrder.setPayValidNum4(BigDecimal.ZERO);

			if (req.getPayType().equals(1)) {
				// USDT：优先分摊锁定余额，其次可用余额
				BigDecimal need = unitPrice;
				BigDecimal useValidNum4 = remainingValidNum4.min(need);
				remainingValidNum4 = remainingValidNum4.subtract(useValidNum4);
				need = need.subtract(useValidNum4);
				BigDecimal useValidNum1 = need.max(BigDecimal.ZERO);
				remainingValidNum1 = remainingValidNum1.subtract(useValidNum1);
				packageOrder.setPayValidNum4(useValidNum4);
				packageOrder.setPayValidNum1(useValidNum1);
				packageOrder.setDfcPrice(BigDecimal.ZERO);
			} else {
				// DFC：按订单等额换算，存在四舍五入误差时由剩余量兜底
				BigDecimal perOrderDfcAmount = unitPrice.divide(paySummary.getDfcPrice(),
					ConstantStatic.newScale, ConstantStatic.roundingModeNew);
				BigDecimal useValidNum2 = remainingValidNum2.min(perOrderDfcAmount);
				remainingValidNum2 = remainingValidNum2.subtract(useValidNum2);
				packageOrder.setPayValidNum2(useValidNum2);
				packageOrder.setDfcPrice(paySummary.getDfcPrice());
			}

			//todo 待实现
			//packageOrder.setMiningNo();
			packageOrder.setOrderNo(orderNo);
			packageOrder.setUserId(userId);
			packageOrder.setRunDays(0);
			packageOrder.setSourceType(0);

			packageOrder.setDayReward(BigDecimal.ZERO);
			packageOrder.setTotalReward(BigDecimal.ZERO);
			packageOrder.setStatus(0);
			packageOrder.setBizStatus(0);
			packageOrder.setBizStatus1(0);
			packageOrder.setCreateTime(new Date());
			orderList.add(packageOrder);
		}
		boolean save = miningPackageOrderService.saveBatch(orderList);
		if (!save) {
			throw new ServiceException(ResponseCode.CODE_1002);
		}

		if(userInfo.getIsValid() == 0){
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getUserId())
				.set(UserInfo::getIsValid,1)
				.update();
		}

		//添加自身业绩
		update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, userId)
			.setSql("performance = performance + " + req.getNum())
			.update();
		if(!update){
			throw new ServiceException(ResponseCode.CODE_1002);
		}
		//直推、团队、小区业绩
		List<Long> parentIds = userInfo.getParentIds();
		if(CollectionUtil.isNotEmpty(parentIds)){
			update = userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.setSql("sub_performance = sub_performance + " + req.getNum())
				.update();
			if(!update){
				throw new ServiceException(ResponseCode.CODE_1002);
			}

			update = userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, parentIds)
				.setSql("umbrella_performance = umbrella_performance + " + req.getNum())
				.update();
			if(!update){
				throw new ServiceException(ResponseCode.CODE_1002);
			}

			//计算小区业绩
			calculateCommunityPerformance(parentIds);
		}

		//后续费的分红信息、和等级计算
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				for (MiningPackageOrder packageOrder : orderList) {
					List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
					OrderMsgDO orderMsgDO = new OrderMsgDO();
					orderMsgDO.setId(packageOrder.getId());
					orderMsgDO.setBizType(1);
					orderMsgDOList.add(orderMsgDO);
					asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
				}
			}
		});
		return ResultPista.success();
	}
*/


/*
	*/
/**
	 * 计算小区业绩
	 * @param parentIds
	 *//*

	private void calculateCommunityPerformance(List<Long> parentIds) {
		// 小区业绩（对所有上级计算：去掉最大直推线）
		Map<Long, BigDecimal> userCommunityPerformanceMap = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, parentIds)
			.select(UserInfo::getUserId,UserInfo::getCommunityPerformance)
			.list().stream().collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getCommunityPerformance, (k1, k2) -> k2));
		if (CollectionUtil.isNotEmpty(parentIds)) {
			for (Long parentId : parentIds) {
				List<UserInfo> children = userInfoService.lambdaQuery()
					.eq(UserInfo::getInviteUserId, parentId)
					.select(UserInfo::getUserId,UserInfo::getUmbrellaPerformance,
						UserInfo::getPerformance)
					.list();
				if (CollectionUtil.isEmpty(children) || children.size() <= 1) {
					if(userCommunityPerformanceMap.get(parentId).compareTo(BigDecimal.ZERO)!=0){
						userInfoService.lambdaUpdate()
							.eq(UserInfo::getUserId, parentId)
							.set(UserInfo::getCommunityPerformance, BigDecimal.ZERO)
							.update();
					}
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

				if(userCommunityPerformanceMap.get(parentId).compareTo(communityPerformance)!=0){
					userInfoService.lambdaUpdate()
						.eq(UserInfo::getUserId, parentId)
						.set(UserInfo::getCommunityPerformance, communityPerformance)
						.update();
				}
			}
		}
	}
*/

	// 矿机订单支付入口，按支付类型分发
	private PaySummary handleMiningPackagePay(Integer payType,
											  BigDecimal totalPrice,
											  UserMoney userInfoMoney,
											  String masterOrderNo,
											  Long userId) {
		if (payType.equals(1)) {
			return handleUsdtPay(totalPrice, userInfoMoney, masterOrderNo, userId);
		} else {
			return handleDfcPay(totalPrice, userInfoMoney, masterOrderNo, userId);
		}
	}

	// USDT 支付：优先扣锁定USDT(validNum4)，不足再扣USDT(validNum1)
	private PaySummary handleUsdtPay(BigDecimal totalPrice,
									 UserMoney userInfoMoney,
									 String masterOrderNo,
									 Long userId) {
		BigDecimal allValidNum1 = userInfoMoney.getValidNum1().add(userInfoMoney.getValidNum4());
		if (allValidNum1.compareTo(totalPrice) < 0) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
		if (userInfoMoney.getValidNum4().compareTo(BigDecimal.ZERO) <= 0) {
			//扣款usdt
			int count = userWalletService.handerUserMoney(totalPrice.negate(), masterOrderNo, userId, userId,
				ConstantType.user_money_log_source_type.type_6, ConstantType.user_money_coin_type.type_1);
			if (count != 1) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
			return new PaySummary(totalPrice, BigDecimal.ZERO, BigDecimal.ZERO, null);
		}
		//扣款锁定usdt完全够支付对应金额情况
		if (userInfoMoney.getValidNum4().compareTo(totalPrice) >= 0) {
			//扣款锁定usdt
			int count = userWalletService.handerUserMoney(totalPrice.negate(), masterOrderNo, userId, userId,
				ConstantType.user_money_log_source_type.type_6, ConstantType.user_money_coin_type.type_4);
			if (count != 1) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
			return new PaySummary(BigDecimal.ZERO, totalPrice, BigDecimal.ZERO, null);
		}
		//支付部分锁定usdt金额+usdt
		BigDecimal subtractValidNum1 = totalPrice.subtract(userInfoMoney.getValidNum4());
		//扣款锁定usdt
		int count = userWalletService.handerUserMoney(userInfoMoney.getValidNum4().negate(), masterOrderNo, userId, userId,
			ConstantType.user_money_log_source_type.type_6, ConstantType.user_money_coin_type.type_4);
		if (count != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
		if (subtractValidNum1.compareTo(BigDecimal.ZERO) > 0) {
			//扣款usdt
			count = userWalletService.handerUserMoney(subtractValidNum1.negate(), masterOrderNo, userId, userId,
				ConstantType.user_money_log_source_type.type_6, ConstantType.user_money_coin_type.type_1);
			if (count != 1) {
				throw new ServiceException(ResponseCode.CODE_1015);
			}
		}
		return new PaySummary(subtractValidNum1, userInfoMoney.getValidNum4(), BigDecimal.ZERO, null);
	}

	// DFC 支付：按价格换算DFC数量并扣款
	private PaySummary handleDfcPay(BigDecimal totalPrice,
									UserMoney userInfoMoney,
									String masterOrderNo,
									Long userId) {
		//获取dfc价格
		BigDecimal dFcPrice = bizCommonService.getDFcPrice();
		BigDecimal dfcValueInUsdt = userInfoMoney.getValidNum2().multiply(dFcPrice)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if (dfcValueInUsdt.compareTo(totalPrice) < 0) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
		//算出要支付多少的dfc
		BigDecimal payDfcAmount = totalPrice
			.divide(dFcPrice, ConstantStatic.newScale, ConstantStatic.roundingModeNew);

		//扣款dfc
		int count = userWalletService.handerUserMoney(payDfcAmount.negate(), masterOrderNo, userId, userId,
			ConstantType.user_money_log_source_type.type_6, ConstantType.user_money_coin_type.type_2);
		if (count != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}
		return new PaySummary(BigDecimal.ZERO, BigDecimal.ZERO, payDfcAmount, dFcPrice);
	}

	@Override
	public MiningPackageDto miningInfo() {
		MiningPackageDto result = new MiningPackageDto();
		MiningPackage miningPackage = miningPackageService.lambdaQuery()
			.eq(MiningPackage::getStatus, 1)
			.one();
		if (miningPackage == null) {
			return result;
		}
		result.setId(miningPackage.getId());
		result.setComputingPower(miningPackage.getRemark());
		result.setPrice(miningPackage.getPrice());
		return result;
	}

	/**
	 * 返回相差几秒，如果当前时间晚于结束时间则返回固定的10秒
	 *
	 * @param current 当前时间
	 * @param endTime 结束时间
	 * @return 相差的秒数
	 */
	public static Long getEndTime(Date current, Date endTime) {
		if (current == null || endTime == null) {
			throw new IllegalArgumentException("时间参数不能为空");
		}

		if (current.after(endTime)) {
			return 10L; // 当前时间晚于结束时间时返回10秒
		} else {
			// 计算时间差（毫秒）
			long diffMillis = endTime.getTime() - current.getTime();
			// 转换为秒
			return diffMillis / 1000;
		}
	}



	private void doBurn(String account, BigDecimal reward, String orderNo) {
		Map<String, Object> formParams = new HashMap<>();
		formParams.put("orderNo", orderNo);
		formParams.put("amount", reward.stripTrailingZeros().toPlainString());
		formParams.put("accountAddress", account);
		formParams.put("tokenAddress", "tokenName");
		formParams.put("sign", SignUtil.getSign(formParams, false, false, md5Key));

		// 使用HttpRequest创建请求对象
		String url = baseUrl + "/api/burn";
		//String url = baseUrl + "/api/mint";
		HttpRequest request = HttpRequest.post(url)
			.form(formParams) // 设置表单参数
			.header("Custom-Header", "HeaderValue") // 设置自定义请求头
			.timeout(5000); // 设置超时时间（毫秒）
		// 发送请求并获取响应

		HttpResponse response;
		try {
			response = request.execute();
		} catch (IORuntimeException ex) {
			log.error("withdrawal request timeout, params:{}, url:{}", formParams, url, ex);
			throw new ServiceException("提现通道请求超时，请稍后重试");
		}

		// 获取响应状态码
		int statusCode = response.getStatus();
		log.info("Status Code:{}", statusCode);

		// 获取响应体
		String responseBody = response.body();
		log.info("responseBody:{}", responseBody);
		JSONObject jsonResponse = JSONUtil.parseObj(response.body());
		Integer code = jsonResponse.getInt("code");
		if (code.equals(200)) {
			//200才是成功
		}
	}


	/**
	 * 只有活期矿机+封闭式基金需要24小时候才到账
	 *
	 * @param miningPackageOrder
	 * @return
	 */
	private void sendDelayOrderMsg(MiningPackageOrder miningPackageOrder) {
		//需要发送到延迟队列去处理 降级相关的逻辑
//			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
//				@Override
//				public void afterCommit() {
//					Date current = DateUtil.date();
//
//					//活期矿机定期矿机都需要t+1到账
//					Long targetTime = getEndTime(current, miningPackageOrder.getReturnedTime());
//					//发送消息到延迟队列
//					redissonDelayHandler.add(new RedissonDelayOrder(miningPackageOrder.getOrderNo(), targetTime, SysConstant.TWO,
//						null, RedisConstant.StreamMsgConstant.DELAY_ORDER_TIMEOUT_QUEUE));
//
//
//					List<OrderMsgDO> orderMsgDOList = new ArrayList<>();
//					OrderMsgDO orderMsgDO = new OrderMsgDO();
//					orderMsgDO.setId(miningPackageOrder.getId());
//					orderMsgDO.setBizType(2);
//					orderMsgDOList.add(orderMsgDO);
//					asyncDynamicOrderSettlementServiceImpl.sendMessage(orderMsgDOList);
//
//				}
//			});
	}

	private static class PaySummary {
		private final BigDecimal payValidNum1;
		private final BigDecimal payValidNum4;
		private final BigDecimal payValidNum2;
		private final BigDecimal dfcPrice;

		private PaySummary(BigDecimal payValidNum1,
						   BigDecimal payValidNum4,
						   BigDecimal payValidNum2,
						   BigDecimal dfcPrice) {
			this.payValidNum1 = payValidNum1 == null ? BigDecimal.ZERO : payValidNum1;
			this.payValidNum4 = payValidNum4 == null ? BigDecimal.ZERO : payValidNum4;
			this.payValidNum2 = payValidNum2 == null ? BigDecimal.ZERO : payValidNum2;
			this.dfcPrice = dfcPrice;
		}

		private BigDecimal getPayValidNum1() {
			return payValidNum1;
		}

		private BigDecimal getPayValidNum4() {
			return payValidNum4;
		}

		private BigDecimal getPayValidNum2() {
			return payValidNum2;
		}

		private BigDecimal getDfcPrice() {
			return dfcPrice;
		}
	}

	/**
	 * 计算订单最大可释放金额（不超过本金）
	 *
	 * @param principal 本金
	 * @param dailyRate 日利率
	 * @param runDays   订单实际运行天数
	 * @return 最大可释放金额
	 */
	public BigDecimal calculateMaxReleasable(BigDecimal principal,
											 BigDecimal dailyRate,
											 int runDays) {
		if (runDays <= 0) {
			return BigDecimal.ZERO;
		}

		// 每日收益
		BigDecimal dailyReward = principal.multiply(dailyRate);

		// 理论最大可释放 = 运行天数 × 每日收益
		BigDecimal theoreticalMax = dailyReward.multiply(new BigDecimal(runDays));

		// 如果超过本金，返回本金；否则返回理论值
		return theoreticalMax.compareTo(principal) > 0 ? principal : theoreticalMax;
	}

}
