package com.xms.web.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xms.common.constant.*;
import com.xms.common.exception.ServiceException;
import com.xms.common.mq.dynamic.OrderMsgDO;
import com.xms.common.result.ResponseCode;
import com.xms.common.utils.spring.SpringUtils;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.domain.*;
import com.xms.dao.entity.bo.ChangeLevelUserBo;
import com.xms.dao.entity.domain.SysPara;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.domain.Withdrawal;
import com.xms.dao.entity.vo.ParentUserTaskVo;
import com.xms.dao.service.*;
import com.xms.dao.service.impl.StakeHostingOrderServiceImpl;
import com.xms.dao.service.impl.StakeOrderServiceImpl;
import com.xms.dao.service.impl.UserInfoServiceImpl;
import com.xms.web.service.IRedisStreamBizJobService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: renengadePISTA
 * @createDate: 2023/8/28
 */
@Service
@AllArgsConstructor
@Slf4j
public class RedisStreamBizJobServiceImpl implements IRedisStreamBizJobService {
	private static final String SQL_VALID_NUM3 = "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num3=valid_num3+?,source_code=?,source_type=?,source_id=? WHERE id=? ";

	private static final String SQL_VALID_NUM1 = "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num1=valid_num1+?,source_code=?,source_type=?,source_id=? WHERE id=? ";
	private static final String SQL_VALID_NUM2 = "UPDATE t_user_money SET update_time=?,gt_id=?,valid_num2=valid_num2+?,source_code=?,source_type=?,source_id=? WHERE id=? ";


	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private IUserLevelConfigService userLevelConfigService;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private UserWalletService userWalletServiceImpl;

	@Autowired
	private IRewardRecordService rewardRecordService;

	@Autowired
	private IMiningPackageOrderService miningPackageOrderService;

	@Autowired
	private IMiningRewardConfigService miningRewardConfigService;

	@Autowired
	private IStakeOrderService stakeOrderService;

	@Autowired
	private IStakeHostingOrderService stakeHostingOrderService;

	@Autowired
	private IStakeHostingWeeklyCommunityPerformanceService stakeHostingWeeklyCommunityPerformanceService;

	@Autowired
	private ISysParaService sysParaService;

	@Autowired
	private UserWalletService userWalletService;

	@Autowired
	private WithdrawalService withdrawalService;

	@Autowired
	private IWithdrawalConfigService withdrawalConfigService;

	@Autowired
	private INodePackageOrderService nodePackageOrderService;

	@Autowired
	private INodePackageService nodePackageService;



	@Override
	@Transactional(rollbackFor = Exception.class)
	public Integer handlerDynamicOrderSettlement(List req) {
		List<OrderMsgDO> ids = BeanUtil.copyToList(req, OrderMsgDO.class);
		log.debug("需要处理的矿机订单 orders:{}", ids);
		if(CollectionUtil.isNotEmpty(ids)) {
			OrderMsgDO orderMsgDO = ids.get(0);
			if(orderMsgDO.getBizType().equals(1)){
				//质押节点订单的处理
				Integer x = handleBizType1(orderMsgDO);
				if (x != null) return x;
			}else if(orderMsgDO.getBizType().equals(2)){
				//提现的业务处理
				handleBizType2(orderMsgDO);
			}else if(orderMsgDO.getBizType().equals(3)){
				//处理领取了空投 需要更改订单状态、计算等级、修改订单状态
				handleBizType3(orderMsgDO);
			}else if(orderMsgDO.getBizType().equals(4)){
				//托管订单支付/拨付/结束后重算小区业绩和真实等级
				handleBizType4(orderMsgDO);
			}else if(orderMsgDO.getBizType().equals(5)){
				//托管订单周新增业绩统计
				stakeHostingWeeklyCommunityPerformanceService.processOrderWeeklyPerformance(orderMsgDO.getId());
			}
		}
		return 1;
	}

	/**
	 * 托管订单等级重算：只根据个人托管业绩和小区托管业绩刷新真实等级。
	 */
	private void handleBizType4(OrderMsgDO orderMsgDO) {
		StakeHostingOrder order = stakeHostingOrderService.lambdaQuery()
			.eq(StakeHostingOrder::getId, orderMsgDO.getId())
			.eq(StakeHostingOrder::getPayStatus, StakeHostingOrderServiceImpl.PAY_SUCCESS)
			.one();
		if (order == null) {
			log.info("托管等级重算跳过，订单不存在或未支付 orderId:{}", orderMsgDO.getId());
			return;
		}
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, order.getUserId())
			.one();
		if (userInfo == null) {
			log.info("托管等级重算跳过，用户不存在 userId:{}", order.getUserId());
			return;
		}
		LinkedHashSet<Long> recalculateUserIds = new LinkedHashSet<>();
		recalculateUserIds.add(userInfo.getUserId());
		List<Long> parentIds = userInfo.getParentIds();
		if (CollectionUtil.isNotEmpty(parentIds)) {
			recalculateUserIds.addAll(parentIds);
		}
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
	 * 处理领取了空投 需要更改订单状态、计算等级、修改订单状态
	 * @param orderMsgDO
	 */
	private void handleBizType3(OrderMsgDO orderMsgDO) {
	}

	/**
	 * 提现的业务处理
	 * @param orderMsgDO
	 */
	private void handleBizType2(OrderMsgDO orderMsgDO) {
		/*Withdrawal withdrawal = withdrawalService.lambdaQuery()
			.eq(Withdrawal::getId, orderMsgDO.getId())
			.eq(Withdrawal::getStatus, 3)
			.eq(Withdrawal::getBizStatus, 0)
			.one();

		if(withdrawal == null){
			return ;
		}

		//系统沉淀手续费率
		if(withdrawal.getSystemFeeRatio().compareTo(BigDecimal.ZERO)>0){
			BigDecimal sysFee = withdrawal.getChangeBalance().multiply(withdrawal.getSystemFeeRatio())
				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)
				.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);

			if(sysFee.compareTo(BigDecimal.ZERO)>0){
				sysParaService.lambdaUpdate()
					.eq(SysPara::getSysParaId,1)
					.setSql("para_value = para_value + " + sysFee)
					.update();
			}

		}
		//代理分红
		if(withdrawal.getAgentDividendFeeRatio().compareTo(BigDecimal.ZERO)>0){
			BigDecimal fee = withdrawal.getChangeBalance().multiply(withdrawal.getAgentDividendFeeRatio())
				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)
				.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			List<UserInfo> userInfoList = userInfoService.list(new QueryWrapper<UserInfo>()
				.select("user_id", "has_active_stake_order", "GREATEST(IFNULL(game_level, 0), IFNULL(min_game_level, 0), IFNULL(admin_game_level, 0)) AS game_level")
				.apply("GREATEST(IFNULL(game_level, 0), IFNULL(min_game_level, 0), IFNULL(admin_game_level, 0)) > 0 AND has_active_stake_order = 1"));
			if(CollectionUtil.isNotEmpty(userInfoList) && fee.compareTo(BigDecimal.ZERO) > 0){
				WithdrawalConfig withdrawalConfig = withdrawalConfigService.lambdaQuery()
					.eq(WithdrawalConfig::getCoinType, withdrawal.getCoinType())
					.one();
				if(withdrawalConfig == null){
					throw new ServiceException("提现配置不存在");
				}
				Map<Integer, List<UserInfo>> levelMap = userInfoList.stream()
					.collect(Collectors.groupingBy(UserInfo::getGameLevel));
				// 虚拟等级 2:区代理,3:县代理,4:省代理
				List<UserInfo> districtAgents = levelMap.getOrDefault(2, Collections.emptyList());
				List<UserInfo> countyAgents = levelMap.getOrDefault(3, Collections.emptyList());
				List<UserInfo> provinceAgents = levelMap.getOrDefault(4, Collections.emptyList());
				// 配置字段语义已修正：区/县/省份数按字段本身直接对应
				int districtShare = defaultShareValue(withdrawalConfig.getDistrictAgentShare());
				int countyShare = defaultShareValue(withdrawalConfig.getCountyAgentShare());
				int provinceShare = defaultShareValue(withdrawalConfig.getProvinceAgentShare());
				int totalShareCount = districtAgents.size() * districtShare
					+ countyAgents.size() * countyShare
					+ provinceAgents.size() * provinceShare;
				if(totalShareCount > 0){
					// 单个用户可分到的奖励 = 代理分红池 * 当前级别份数 / 全部代理总份数
					BigDecimal districtUserReward = calculateAgentDividendUserReward(fee, districtShare, totalShareCount);
					BigDecimal countyUserReward = calculateAgentDividendUserReward(fee, countyShare, totalShareCount);
					BigDecimal provinceUserReward = calculateAgentDividendUserReward(fee, provinceShare, totalShareCount);
					List<UserMoney> userMoneyValidNum1List = new ArrayList<>(userInfoList.size() > 1000 ? 1000 : userInfoList.size());
					List<RewardRecord> rewardRecordList = new ArrayList<>(userInfoList.size() > 1000 ? 1000 : userInfoList.size());
					Set<Long> exitStakeUserIds = new HashSet<>();
					int stakeCount1 = 0;
					int batchSize = 1000;
					// 按区代理、县代理、省代理三批写入，便于后续分别设置不同来源类型。
					stakeCount1 = appendWithdrawalAgentDividendBatch(districtAgents, districtUserReward, withdrawal,
						userMoneyValidNum1List, rewardRecordList, withdrawal.getCoinType(), 2, stakeCount1, batchSize, exitStakeUserIds);
					stakeCount1 = appendWithdrawalAgentDividendBatch(countyAgents, countyUserReward, withdrawal,
						userMoneyValidNum1List, rewardRecordList, withdrawal.getCoinType(), 3, stakeCount1, batchSize, exitStakeUserIds);
					stakeCount1 = appendWithdrawalAgentDividendBatch(provinceAgents, provinceUserReward, withdrawal,
						userMoneyValidNum1List, rewardRecordList, withdrawal.getCoinType(), 4, stakeCount1, batchSize, exitStakeUserIds);
					if (CollectionUtil.isNotEmpty(userMoneyValidNum1List)) {
						bachUpdateMoneyValid1(userMoneyValidNum1List, withdrawal.getCoinType());
					}
					if (CollectionUtil.isNotEmpty(rewardRecordList)) {
						rewardRecordService.saveBatch(rewardRecordList);
					}
					if(CollectionUtil.isNotEmpty(exitStakeUserIds)){
						refreshUserAndParentLevels(exitStakeUserIds);
					}
				}
			}
		}

		boolean update = withdrawalService.lambdaUpdate()
			.eq(Withdrawal::getId, withdrawal.getId())
			.eq(Withdrawal::getBizStatus, 0)
			.set(Withdrawal::getBizStatus, 1)
			.set(Withdrawal::getUpdateTime, new Date())
			.update();

		if(!update){
			throw new ServiceException("提现订单更新失败");
		}*/
	}

	/**
	 * 组装提现代理分红批处理数据。
	 * 同时组装用户资产变更和奖金记录，最后统一批量落库。
	 * 代理分红发放前，先按收益接收人的进行中质押订单扣减 remainingOutAmount。
	 * agentLevel: 2=区代理, 3=县代理, 4=省代理
	 */
	private int appendWithdrawalAgentDividendBatch(List<UserInfo> agentList, BigDecimal userReward, Withdrawal withdrawal,
												   List<UserMoney> userMoneyValidNum1List, List<RewardRecord> rewardRecordList,
												   Integer rewardCoinType,
												   Integer agentLevel, int stakeCount1, int batchSize,
												   Set<Long> exitStakeUserIds) {
		if(CollectionUtil.isEmpty(agentList) || userReward == null || userReward.compareTo(BigDecimal.ZERO) <= 0){
			return stakeCount1;
		}
		Integer moneySourceType = ConstantType.user_money_log_source_type.type_3;
		Integer rewardSourceType = ConstantType.xms_reward_record_source_type.type_3;
		if(agentLevel == 2){
			moneySourceType = ConstantType.user_money_log_source_type.type_3;
			rewardSourceType = ConstantType.xms_reward_record_source_type.type_3;
		}else if(agentLevel == 3){
			moneySourceType = ConstantType.user_money_log_source_type.type_6;
			rewardSourceType = ConstantType.xms_reward_record_source_type.type_4;

		}else if(agentLevel == 4){
			moneySourceType = ConstantType.user_money_log_source_type.type_7;
			rewardSourceType = ConstantType.xms_reward_record_source_type.type_5;

		}
		for (UserInfo agent : agentList) {
			// 提现代理分红参照分享奖逻辑：先扣当前代理用户未出局质押订单的剩余可产出。
			BigDecimal actualRewardAmount = deductStakeRemainingOutAmount(agent.getUserId(), userReward, exitStakeUserIds);
			if(actualRewardAmount.compareTo(BigDecimal.ZERO) <= 0){
				continue;
			}
			UserMoney entity = new UserMoney();
			entity.setId(agent.getUserId());
			entity.setValidNum1(actualRewardAmount);
			entity.setGtId(IDUtils.getSnowflakeStr());
			entity.setSourceCode(withdrawal.getCode());
			entity.setSourceId(withdrawal.getUserId());
			entity.setSourceType(moneySourceType);
			entity.setUpdateTime(new Date());
			userMoneyValidNum1List.add(entity);

			RewardRecord rewardRecordEntity = new RewardRecord();
			rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
			rewardRecordEntity.setUserId(agent.getUserId());
			rewardRecordEntity.setAmount(actualRewardAmount);
			rewardRecordEntity.setCoinType(rewardCoinType);
			rewardRecordEntity.setSourceType(rewardSourceType);
			rewardRecordEntity.setSourceUserId(withdrawal.getUserId());
			rewardRecordEntity.setSourceOrderCode(withdrawal.getCode());
			rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
			rewardRecordList.add(rewardRecordEntity);

			stakeCount1++;
			if (stakeCount1 >= batchSize) {
				bachUpdateMoneyValid1(userMoneyValidNum1List, rewardCoinType);
				userMoneyValidNum1List.clear();
				if (CollectionUtil.isNotEmpty(rewardRecordList)) {
					rewardRecordService.saveBatch(rewardRecordList);
					rewardRecordList.clear();
				}
				log.info("提现代理分红批量更新成功");
				stakeCount1 = 0;
			}
		}
		return stakeCount1;
	}

	private BigDecimal calculateAgentDividendUserReward(BigDecimal totalFee, int levelShare, int totalShareCount) {
		if(totalFee == null || totalFee.compareTo(BigDecimal.ZERO) <= 0 || levelShare <= 0 || totalShareCount <= 0){
			return BigDecimal.ZERO;
		}
		return totalFee.multiply(BigDecimal.valueOf(levelShare))
			.divide(BigDecimal.valueOf(totalShareCount), ConstantStatic.newScale, ConstantStatic.roundingModeNew);
	}

	private int defaultShareValue(Integer shareValue) {
		return shareValue == null || shareValue <= 0 ? 0 : shareValue;
	}

	/**
	 * 处理购买节点的订单任务
	 * @param orderMsgDO
	 * @return
	 */
	@Nullable
	private Integer handleBizType1(OrderMsgDO orderMsgDO) {
		NodePackageOrder packageOrder = nodePackageOrderService.lambdaQuery()
			.eq(NodePackageOrder::getId,orderMsgDO.getId())
			.eq(NodePackageOrder::getStatus, 1)
			.eq(NodePackageOrder::getBizStatus,0)
			.one();
		if(packageOrder==null){
			return null;
		}
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, packageOrder.getUserId())
			.one();
		//更新团队节点信息+发奖励
		if(userInfo.getInviteUserId()!=null){
			userInfoService.lambdaUpdate()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.setSql("sub_node_performance = sub_node_performance + 1")
				.update();
			userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, userInfo.getParentIds())
				.setSql("node_team_performance = node_team_performance + 1")
				.update();
			//发放直推、间推、奖励
			UserInfo inviteUserInfo = userInfoService.lambdaQuery()
				.eq(UserInfo::getUserId, userInfo.getInviteUserId())
				.one();
			if(inviteUserInfo.getNodeLevel()>0){
				NodePackage directReferralPackage = nodePackageService.lambdaQuery()
					.eq(NodePackage::getId, inviteUserInfo.getNodeLevel())
					.one();
				if(directReferralPackage.getDirectReferralRate().compareTo(BigDecimal.ZERO)>0){
					BigDecimal directReward = directReferralPackage.getDirectReferralRate()
						.multiply(packageOrder.getOrderValueUsdt())
						.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)
						.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
					if(directReward.compareTo(BigDecimal.ZERO)>0){
						//直推奖励
						int count = userWalletServiceImpl.handerUserMoney(directReward, packageOrder.getOrderNo(),
							inviteUserInfo.getUserId(), packageOrder.getUserId(), ConstantType.user_money_log_source_type.type_1,
							ConstantType.user_money_coin_type.type_1);
						if (count != 1) {
							throw new ServiceException(ResponseCode.CODE_1015);
						}
						//奖金记录-直推奖励
						RewardRecord rewardRecordEntity = new RewardRecord();
						rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
						rewardRecordEntity.setUserId(inviteUserInfo.getUserId());
						rewardRecordEntity.setAmount(directReward);
						rewardRecordEntity.setCoinType(ConstantType.user_money_coin_type.type_1);
						rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_1);
						rewardRecordEntity.setSourceUserId(packageOrder.getUserId());
						rewardRecordEntity.setSourceOrderCode(packageOrder.getOrderNo());
						rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
						rewardRecordEntity.setCreateTime(new Date());
						rewardRecordService.save(rewardRecordEntity);
					}
				}
			}

			//间推奖励
			if(inviteUserInfo.getInviteUserId()!=null){
				UserInfo indirectUserInfo = userInfoService.lambdaQuery()
					.eq(UserInfo::getUserId, inviteUserInfo.getInviteUserId())
					.one();

				if(indirectUserInfo.getNodeLevel()>0){
					NodePackage nodePackage = nodePackageService.lambdaQuery()
						.eq(NodePackage::getId, indirectUserInfo.getNodeLevel())
						.one();
					log.info("间推比例: indirectUserOrder:{}",nodePackage);
					if(nodePackage.getIndirectReferralRate().compareTo(BigDecimal.ZERO)>0){
						BigDecimal indirectReward = nodePackage.getIndirectReferralRate().multiply(packageOrder.getOrderValueUsdt())
							.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew)
							.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew);
						if(indirectReward.compareTo(BigDecimal.ZERO)>0){
							//间推奖励
							int count = userWalletServiceImpl.handerUserMoney(indirectReward, packageOrder.getOrderNo(),
								indirectUserInfo.getUserId(), packageOrder.getUserId(), ConstantType.user_money_log_source_type.type_2,
								ConstantType.user_money_coin_type.type_1);
							if (count != 1) {
								throw new ServiceException(ResponseCode.CODE_1015);
							}

							//奖金记录-间推奖励
							RewardRecord rewardRecordEntity = new RewardRecord();
							rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
							rewardRecordEntity.setUserId(indirectUserInfo.getUserId());
							rewardRecordEntity.setAmount(indirectReward);
							rewardRecordEntity.setCoinType(ConstantType.user_money_coin_type.type_1);
							rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_2);
							rewardRecordEntity.setSourceUserId(packageOrder.getUserId());
							rewardRecordEntity.setSourceOrderCode(packageOrder.getOrderNo());
							rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
							rewardRecordEntity.setCreateTime(new Date());
							rewardRecordService.save(rewardRecordEntity);
						}
					}
				}
			}
		}

		//更改订单状态
		boolean update1 = nodePackageOrderService.lambdaUpdate()
			.eq(NodePackageOrder::getId, packageOrder.getId())
			.eq(NodePackageOrder::getBizStatus, 0)
			.set(NodePackageOrder::getBizStatus, 1)
			.update();
		if(!update1){
			log.info("处理节点订单失败,可能已经更新过了.请稍后再试");
		}
		return null;
	}

	/*private Integer handleBizType11(OrderMsgDO orderMsgDO) {
		//正常购买矿机回调处理
		MiningPackageOrder queryOrder = miningPackageOrderService.lambdaQuery()
			.eq(MiningPackageOrder::getId, orderMsgDO.getId())
			.eq(MiningPackageOrder::getBizStatus,0)
			.one();
		if(queryOrder==null){
			log.info("矿机订单已经处理 订单id:{}", orderMsgDO.getId());
			return 1;
		}
		//订单奖励
		BigDecimal orderReward = queryOrder.getPayType() == 1?queryOrder.getOrderValueUsdt():queryOrder.getPayValidNum2();
		Integer rewardCoinType = queryOrder.getPayType() == 1?ConstantType.user_money_coin_type.type_1:ConstantType.user_money_coin_type.type_2;
		//查询直推、间推比例
		//1:直推,2:间推,3:市代理,4:省代理,5:全国代理
		Map<Integer, BigDecimal> rewardMap = miningRewardConfigService.lambdaQuery()
			.list().stream()
			.map(config->{
				config.setRewardRatio(config.getRewardRatio().divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew));
				return config;
			})
			.collect(Collectors.toMap(MiningRewardConfig::getRewardLevel, MiningRewardConfig::getRewardRatio, (k1, k2) -> k2));
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, queryOrder.getUserId())
			.one();
		if(userInfo.getInviteUserId()!=null){
			// 按邀请链向上查找最近两个有效上级：第一个有效用户发直推，第二个有效用户发间推
			List<ParentUserTaskVo> validParentUsers = userInfoService.getValidParentUserTaskVo(userInfo.getUserId(), 2);
			if(CollectionUtil.isNotEmpty(validParentUsers)){
				ParentUserTaskVo directUser = validParentUsers.get(0);
				// 最近的有效上级作为直推奖励接收人
				grantInviteReward(directUser.getUserId(), rewardMap.get(1), orderReward, queryOrder, userInfo.getUserId(),
					rewardCoinType, ConstantType.user_money_log_source_type.type_8,
					ConstantType.xms_reward_record_source_type.type_10);
				if(validParentUsers.size() > 1){
					ParentUserTaskVo indirectUser = validParentUsers.get(1);
					// 在直推上级之上继续找到的下一个有效用户作为间推奖励接收人
					grantInviteReward(indirectUser.getUserId(), rewardMap.get(2), orderReward, queryOrder, userInfo.getUserId(),
						rewardCoinType, ConstantType.user_money_log_source_type.type_9,
						ConstantType.xms_reward_record_source_type.type_11);
				}
			}
		}

		//查询市代、省代、全国代人数
		List<UserInfo> userInfoList = userInfoService.list(new QueryWrapper<UserInfo>()
			.select("user_id", "is_valid","GREATEST(IFNULL(game_level, 0), IFNULL(min_game_level, 0), IFNULL(admin_game_level, 0)) AS game_level")
			// 实际等级：取真实等级、赠送等级、管理员保底等级的最大值
			.apply("GREATEST(IFNULL(game_level, 0), IFNULL(min_game_level, 0), IFNULL(admin_game_level, 0)) IN (3,4,5)"));
		if(CollectionUtil.isNotEmpty(userInfoList)){
			Map<Integer, List<UserInfo>> levelMap = userInfoList.stream()
				.collect(Collectors.groupingBy(UserInfo::getGameLevel));
			List<UserMoney> userMoneyValidNum1List = new ArrayList<>(userInfoList.size()>1000?1000:userInfoList.size());
			List<RewardRecord> rewardRecordList = new ArrayList<>(1000);
			RewardRecord rewardRecordEntity = null;
			UserMoney entity = null;
			int stakeCount1 = 0;
			int batchSize = 1000;
			List<UserInfo> cityAgents = levelMap.getOrDefault(3, Collections.emptyList());
			List<UserInfo> provinceAgents = levelMap.getOrDefault(4, Collections.emptyList());
			List<UserInfo> nationAgents = levelMap.getOrDefault(5, Collections.emptyList());
			if(CollectionUtil.isNotEmpty(cityAgents)){
				BigDecimal cityAgentsReward = rewardMap.get(3).multiply(orderReward)
					.setScale(ConstantStatic.newScale,ConstantStatic.roundingModeNew);
				if(cityAgentsReward.compareTo(BigDecimal.ZERO)>0){
					//奖励大于0才发放
					BigDecimal userReward = cityAgentsReward.divide
						(new BigDecimal(cityAgents.size()), ConstantStatic.newScale, ConstantStatic.roundingModeNew);
					if(userReward.compareTo(BigDecimal.ZERO)>0){
						for (UserInfo cityAgent : cityAgents) {
							if(cityAgent.getIsValid() == 1){
								entity = new UserMoney();
								entity.setId(cityAgent.getUserId());
								entity.setValidNum1(userReward);
								entity.setGtId(IDUtils.getSnowflakeStr());
								entity.setSourceCode(queryOrder.getOrderNo());
								entity.setSourceId(queryOrder.getUserId());
								entity.setSourceType(ConstantType.user_money_log_source_type.type_10);
								entity.setUpdateTime(new Date());
								userMoneyValidNum1List.add(entity);
								stakeCount1++;
								if (stakeCount1 >= batchSize) {
									bachUpdateMoneyValid1(userMoneyValidNum1List, rewardCoinType);
									userMoneyValidNum1List.clear();
									log.info("更新成功");
									stakeCount1 = 0;
								}

								rewardRecordEntity = new RewardRecord();
								rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
								rewardRecordEntity.setUserId(cityAgent.getUserId());
								rewardRecordEntity.setAmount(userReward);
								rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_12);
								rewardRecordEntity.setCoinType(rewardCoinType);
								rewardRecordEntity.setSourceUserId(userInfo.getUserId());
								rewardRecordEntity.setSourceOrderCode(queryOrder.getOrderNo());
								rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
								rewardRecordList.add(rewardRecordEntity);
							}
						}
					}
				}

			}

			if(CollectionUtil.isNotEmpty(provinceAgents)){
				BigDecimal provinceAgentsReward = rewardMap.get(4).multiply(orderReward)
					.setScale(ConstantStatic.newScale,ConstantStatic.roundingModeNew);
				if(provinceAgentsReward.compareTo(BigDecimal.ZERO)>0){
					//奖励大于0才发放
					BigDecimal userReward = provinceAgentsReward.divide
						(new BigDecimal(provinceAgents.size()), ConstantStatic.newScale, ConstantStatic.roundingModeNew);
					if(userReward.compareTo(BigDecimal.ZERO)>0){
						for (UserInfo provinceAgent : provinceAgents) {
							if(provinceAgent.getIsValid() == 1){
								entity = new UserMoney();
								entity.setId(provinceAgent.getUserId());
								entity.setValidNum1(userReward);
								entity.setGtId(IDUtils.getSnowflakeStr());
								entity.setSourceCode(queryOrder.getOrderNo());
								entity.setSourceId(queryOrder.getUserId());
								entity.setSourceType(ConstantType.user_money_log_source_type.type_11);
								entity.setUpdateTime(new Date());
								userMoneyValidNum1List.add(entity);
								stakeCount1++;
								if (stakeCount1 >= batchSize) {
									bachUpdateMoneyValid1(userMoneyValidNum1List, rewardCoinType);
									userMoneyValidNum1List.clear();
									log.info("更新成功");
									stakeCount1 = 0;
								}

								rewardRecordEntity = new RewardRecord();
								rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
								rewardRecordEntity.setUserId(provinceAgent.getUserId());
								rewardRecordEntity.setAmount(userReward);
								rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_13);
								rewardRecordEntity.setCoinType(rewardCoinType);
								rewardRecordEntity.setSourceUserId(userInfo.getUserId());
								rewardRecordEntity.setSourceOrderCode(queryOrder.getOrderNo());
								rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
								rewardRecordList.add(rewardRecordEntity);
							}

						}
					}
				}


			}

			if(CollectionUtil.isNotEmpty(nationAgents)){
				BigDecimal nationAgentsReward = rewardMap.get(5).multiply(orderReward)
					.setScale(ConstantStatic.newScale,ConstantStatic.roundingModeNew);
				if(nationAgentsReward.compareTo(BigDecimal.ZERO)>0){
					BigDecimal userReward = nationAgentsReward.divide
						(new BigDecimal(nationAgents.size()), ConstantStatic.newScale, ConstantStatic.roundingModeNew);
					if(userReward.compareTo(BigDecimal.ZERO)>0){
						for (UserInfo nationAgent : nationAgents) {
							if(nationAgent.getIsValid() == 1){
								entity = new UserMoney();
								entity.setId(nationAgent.getUserId());
								entity.setValidNum1(userReward);
								entity.setGtId(IDUtils.getSnowflakeStr());
								entity.setSourceCode(queryOrder.getOrderNo());
								entity.setSourceId(queryOrder.getUserId());
								entity.setSourceType(ConstantType.user_money_log_source_type.type_12);
								entity.setUpdateTime(new Date());
								userMoneyValidNum1List.add(entity);
								stakeCount1++;
								if (stakeCount1 >= batchSize) {
									bachUpdateMoneyValid1(userMoneyValidNum1List, rewardCoinType);
									userMoneyValidNum1List.clear();
									log.info("更新成功");
									stakeCount1 = 0;
								}

								rewardRecordEntity = new RewardRecord();
								rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
								rewardRecordEntity.setUserId(nationAgent.getUserId());
								rewardRecordEntity.setAmount(userReward);
								rewardRecordEntity.setSourceType(ConstantType.xms_reward_record_source_type.type_14);
								rewardRecordEntity.setCoinType(rewardCoinType);
								rewardRecordEntity.setSourceUserId(userInfo.getUserId());
								rewardRecordEntity.setSourceOrderCode(queryOrder.getOrderNo());
								rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
								rewardRecordList.add(rewardRecordEntity);
							}
						}
					}
				}
			}

			//修改资产
			if (CollectionUtil.isNotEmpty(userMoneyValidNum1List)) {
				bachUpdateMoneyValid1(userMoneyValidNum1List, rewardCoinType);
			}

			//插入v1资产
			if (CollectionUtil.isNotEmpty(rewardRecordList)) {
				rewardRecordService.saveBatch(rewardRecordList);
			}
		}

		boolean update = miningPackageOrderService.lambdaUpdate()
			.eq(MiningPackageOrder::getId, queryOrder.getId())
			.eq(MiningPackageOrder::getBizStatus, 0)
			.set(MiningPackageOrder::getBizStatus, 1)
			.set(MiningPackageOrder::getUpdateTime, new Date())
			.update();
		if(!update){
			throw new ServiceException("更新矿机订单信息失败");
		}

		//处理等级
		List<Long> parentIds = userInfo.getParentIds();
		if(CollectionUtil.isNotEmpty(parentIds)){
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
				callUserLevel(info, userLevelConfigList);
			}
		}
		return null;
	}*/

	private void grantStakeInviteReward(Long rewardUserId, BigDecimal rewardAmount, String sourceOrderNo,
										Long sourceUserId, Integer userMoneySourceType,
										Integer rewardCoinType, Integer rewardRecordSourceType) {
		if(rewardUserId == null || rewardAmount == null || rewardAmount.compareTo(BigDecimal.ZERO) <= 0){
			return;
		}
		BigDecimal actualRewardAmount = deductStakeRemainingOutAmount(rewardUserId, rewardAmount);
		if(actualRewardAmount.compareTo(BigDecimal.ZERO) <= 0){
			return;
		}
		int count = userWalletService.handerUserMoney(actualRewardAmount, sourceOrderNo,
			rewardUserId, sourceUserId, userMoneySourceType, rewardCoinType);
		if (count != 1) {
			throw new ServiceException(ResponseCode.CODE_1015);
		}

		RewardRecord rewardRecordEntity = new RewardRecord();
		rewardRecordEntity.setOrderCode(IDUtils.getSnowflakeStr());
		rewardRecordEntity.setUserId(rewardUserId);
		rewardRecordEntity.setAmount(actualRewardAmount);
		rewardRecordEntity.setSourceType(rewardRecordSourceType);
		rewardRecordEntity.setCoinType(rewardCoinType);
		rewardRecordEntity.setSourceUserId(sourceUserId);
		rewardRecordEntity.setSourceOrderCode(sourceOrderNo);
		rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
		rewardRecordService.save(rewardRecordEntity);
	}

	private BigDecimal deductStakeRemainingOutAmount(Long rewardUserId, BigDecimal rewardAmount) {
		return deductStakeRemainingOutAmount(rewardUserId, rewardAmount, null);
	}

	private BigDecimal deductStakeRemainingOutAmount(Long rewardUserId, BigDecimal rewardAmount, Set<Long> exitStakeUserIds) {
		// 按收益接收人的进行中质押订单处理，遵循先到先扣。
		List<StakeOrder> activeStakeOrders = stakeOrderService.lambdaQuery()
			.eq(StakeOrder::getStatus, 1)
			.eq(StakeOrder::getUserId, rewardUserId)
			.orderByAsc(StakeOrder::getId)
			.list();
		if(CollectionUtil.isEmpty(activeStakeOrders)){
			return BigDecimal.ZERO;
		}
		BigDecimal remainingRewardAmount = rewardAmount;
		BigDecimal actualRewardAmount = BigDecimal.ZERO;
		Date now = new Date();
		List<StakeOrder> updateStakeOrders = new ArrayList<>();
		Map<Long, Integer> finishOrderCountMap = new HashMap<>();
		Map<Long, BigDecimal> performanceReduceMap = new HashMap<>();
		// 这里依赖事务和顺序队列串行消费，不额外使用 for update 行锁。
		for (StakeOrder stakeOrder : activeStakeOrders) {
			BigDecimal orderRemainingOutAmount = getOrderRemainingOutAmount(stakeOrder);
			if(orderRemainingOutAmount.compareTo(BigDecimal.ZERO) <= 0){
				stakeOrder.setRemainingOutAmount(BigDecimal.ZERO);
				stakeOrder.setStatus(2);
				stakeOrder.setUpdateTime(now);
				updateStakeOrders.add(stakeOrder);
				recordFinishedStakeOrder(finishOrderCountMap, performanceReduceMap, rewardUserId, stakeOrder);
				continue;
			}
			if(remainingRewardAmount.compareTo(BigDecimal.ZERO) <= 0){
				break;
			}
			BigDecimal deductAmount = orderRemainingOutAmount.min(remainingRewardAmount);
			BigDecimal newRemainingOutAmount = orderRemainingOutAmount.subtract(deductAmount)
				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			stakeOrder.setRemainingOutAmount(newRemainingOutAmount);
			if(newRemainingOutAmount.compareTo(BigDecimal.ZERO) <= 0){
				stakeOrder.setStatus(2);
				recordFinishedStakeOrder(finishOrderCountMap, performanceReduceMap, rewardUserId, stakeOrder);
			}
			stakeOrder.setUpdateTime(now);
			updateStakeOrders.add(stakeOrder);
			actualRewardAmount = actualRewardAmount.add(deductAmount)
				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
			remainingRewardAmount = remainingRewardAmount.subtract(deductAmount)
				.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		}
		if(CollectionUtil.isNotEmpty(updateStakeOrders)){
			boolean update = stakeOrderService.updateBatchById(updateStakeOrders);
			if(!update){
				throw new ServiceException("更新质押订单剩余可产出失败");
			}
		}
		if(CollectionUtil.isNotEmpty(finishOrderCountMap)){
			if(exitStakeUserIds != null){
				exitStakeUserIds.add(rewardUserId);
			}
			boolean hasActiveStakeOrder = activeStakeOrders.stream()
				.anyMatch(order -> Integer.valueOf(1).equals(order.getStatus())
					&& getOrderRemainingOutAmount(order).compareTo(BigDecimal.ZERO) > 0);
			refreshStakeExitUserInfo(rewardUserId, performanceReduceMap, hasActiveStakeOrder);
		}
		return actualRewardAmount;
	}

	private void refreshUserAndParentLevels(Set<Long> exitStakeUserIds) {
		if(CollectionUtil.isEmpty(exitStakeUserIds)){
			return;
		}
		List<UserInfo> exitUserInfoList = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, exitStakeUserIds)
			.select(UserInfo::getUserId, UserInfo::getParentChain)
			.list();
		if(CollectionUtil.isEmpty(exitUserInfoList)){
			return;
		}
		Set<Long> recalculateUserIds = new HashSet<>();
		for (UserInfo userInfo : exitUserInfoList) {
			recalculateUserIds.add(userInfo.getUserId());
			List<Long> parentIds = userInfo.getParentIds();
			if(CollectionUtil.isNotEmpty(parentIds)){
				recalculateUserIds.addAll(parentIds);
			}
		}
		if(CollectionUtil.isEmpty(recalculateUserIds)){
			return;
		}
		List<UserInfo> parentUserInfoList = userInfoService.lambdaQuery()
			.in(UserInfo::getUserId, recalculateUserIds)
			.orderByDesc(UserInfo::getUserId)
			.list();
		List<UserLevelConfig> userLevelConfigList = userLevelConfigService.lambdaQuery()
			.gt(UserLevelConfig::getLevel,0)
			.orderByAsc(UserLevelConfig::getLevel)
			.list();
		for (UserInfo info : parentUserInfoList) {
			SpringUtils.getBean(StakeOrderServiceImpl.class).callUserLevel(info, userLevelConfigList);
		}
	}

	private void recordFinishedStakeOrder(Map<Long, Integer> finishOrderCountMap, Map<Long, BigDecimal> performanceReduceMap,
										  Long rewardUserId, StakeOrder stakeOrder) {
		if(rewardUserId == null || stakeOrder == null){
			return;
		}
		finishOrderCountMap.merge(rewardUserId, 1, Integer::sum);
		BigDecimal stakeAmount = stakeOrder.getStakeUsdtAmount() == null ? BigDecimal.ZERO : stakeOrder.getStakeUsdtAmount();
		performanceReduceMap.merge(rewardUserId, stakeAmount, BigDecimal::add);
	}

	private void refreshStakeExitUserInfo(Long rewardUserId, Map<Long, BigDecimal> performanceReduceMap, boolean hasActiveStakeOrder) {
		UserInfo userInfo = userInfoService.lambdaQuery()
			.eq(UserInfo::getUserId, rewardUserId)
			.select(UserInfo::getUserId, UserInfo::getParentChain)
			.one();
		if(userInfo == null){
			throw new ServiceException("用户信息不存在");
		}
		BigDecimal reduceAmount = performanceReduceMap.getOrDefault(rewardUserId, BigDecimal.ZERO)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		boolean update = userInfoService.lambdaUpdate()
			.eq(UserInfo::getUserId, rewardUserId)
			.setSql("performance = GREATEST(IFNULL(performance, 0) - " + reduceAmount.toPlainString() + ", 0)")
			.update();
		if(!update){
			throw new ServiceException("更新用户质押业绩失败");
		}
		List<Long> parentIds = userInfo.getParentIds();
		if(CollectionUtil.isNotEmpty(parentIds) && reduceAmount.compareTo(BigDecimal.ZERO) > 0){
			update = userInfoService.lambdaUpdate()
				.in(UserInfo::getUserId, parentIds)
				.setSql("umbrella_performance = GREATEST(IFNULL(umbrella_performance, 0) - " + reduceAmount.toPlainString() + ", 0)")
				.update();
			if(!update){
				throw new ServiceException("更新团队质押业绩失败");
			}
		}
	}

	private BigDecimal getOrderRemainingOutAmount(StakeOrder stakeOrder) {
		if(stakeOrder.getRemainingOutAmount() != null){
			return stakeOrder.getRemainingOutAmount();
		}
		if(stakeOrder.getAllOutAmount() != null){
			return stakeOrder.getAllOutAmount();
		}
		return BigDecimal.ZERO;
	}

	private void grantInviteReward(Long rewardUserId, BigDecimal rewardRatio, BigDecimal orderReward,
								   MiningPackageOrder queryOrder, Long sourceUserId, Integer rewardCoinType,
								   Integer userMoneySourceType, Integer rewardRecordSourceType) {
		if(rewardUserId == null || rewardRatio == null){
			return;
		}
		BigDecimal rewardAmount = rewardRatio.multiply(orderReward)
			.setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		if(rewardAmount.compareTo(BigDecimal.ZERO) <= 0){
			return;
		}
		int count = userWalletServiceImpl.handerUserMoney(rewardAmount, queryOrder.getOrderNo(),
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
		rewardRecordEntity.setSourceOrderCode(queryOrder.getOrderNo());
		rewardRecordEntity.setGtId(IDUtils.getSnowflakeStr());
		rewardRecordService.save(rewardRecordEntity);
	}

//	/**
//	 * 计算用户等级
//	 * @param info
//	 * @param userLevelConfigList
//	 */
//	public void callUserLevel(UserInfo info, List<UserLevelConfig> userLevelConfigList) {
//		Integer origLevel = info.getGameLevel();
//		Integer currentLevel = 0;
//		if(info.getPerformance().compareTo(BigDecimal.ZERO)>0){
//			// 计算直推线数量
//			List<UserInfo> directUserList = userInfoService.lambdaQuery()
//				.eq(UserInfo::getInviteUserId, info.getUserId())
//				.select(UserInfo::getUserId,UserInfo::getGameLevel,UserInfo::getPerformance,
//					UserInfo::getMaxLegPerformance,UserInfo::getUmbrellaPerformance)
//				.list();
//			Map<Long, List<UserInfo>> childUserListMap = new HashMap<>();
//			for (UserLevelConfig userLevelConfig : userLevelConfigList) {
//				// 大区业绩+小区业绩+自身业绩满足档位条件
//				//计算大区业绩
//				if(info.getMaxLegPerformance().compareTo(userLevelConfig.getTeamPerformance())>=0
//					//小区业绩
//					&& info.getCommunityPerformance().compareTo(userLevelConfig.getCommunityPerformance())>=0
//					//自身业绩大于1
//					&& info.getPerformance().compareTo(userLevelConfig.getPerformance())>=0){
//					if(userLevelConfig.getLevel()>1){
//						if(CollectionUtil.isEmpty(directUserList) || directUserList.size()<userLevelConfig.getRequiredLegNum()){
//							continue;
//						}
//
//						Integer legLevelMin = userLevelConfig.getLegLevelMin();
//						Integer legLevelCount = userLevelConfig.getLegLevelCount();
//						int hitLines = 0;
//						// 逐条线统计是否满足“该等级人数”要求
//						for (UserInfo childUser : directUserList) {
//							List<UserInfo> childUserList = childUserListMap.computeIfAbsent(childUser.getUserId(), userId -> {
//								List<UserInfo> list = userInfoService.getChildUserList(userId);
//								if (list == null) {
//									list = new ArrayList<>();
//								}
//								// 该线包含直推本人
//								list.add(childUser);
//								return list;
//							});
//							long matchCount = childUserList.stream()
//								.filter(u -> u.getGameLevel() != null && legLevelMin != null && u.getGameLevel() >= legLevelMin)
//								.count();
//							// 该线满足人数要求则计为命中
//							if (legLevelCount == null || matchCount >= legLevelCount) {
//								hitLines++;
//							}
//							// 命中线数已满足要求，提前结束
//							if (hitLines >= userLevelConfig.getRequiredLegNum()) {
//								break;
//							}
//						}
//						// 命中线数不足，跳过该档位
//						if (hitLines < userLevelConfig.getRequiredLegNum()) {
//							continue;
//						}
//					}
//					// 记录当前满足的最高档位
//					currentLevel = userLevelConfig.getLevel();
//				}
//			}
//		}
//
//		if(currentLevel !=origLevel){
//			userInfoService.lambdaUpdate()
//				.eq(UserInfo::getUserId, info.getUserId())
//				.set(UserInfo::getGameLevel, currentLevel)
//				.update();
//		}
//	}





	/**
	 * 对佣金钱包资产增加
	 *
	 * @param userMoneyList
	 */
	private void bachUpdateMoneyValid3(List<UserMoney> userMoneyList) {
		int[] ints = jdbcTemplate.batchUpdate(SQL_VALID_NUM3, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				ps.setTimestamp(1, new java.sql.Timestamp(userMoneyList.get(i).getUpdateTime().getTime()));
				ps.setString(2, userMoneyList.get(i).getGtId());
				ps.setBigDecimal(3, userMoneyList.get(i).getValidNum3());
				ps.setString(4, userMoneyList.get(i).getSourceCode());
				ps.setInt(5, userMoneyList.get(i).getSourceType());
				ps.setLong(6, userMoneyList.get(i).getSourceId());
				ps.setLong(7, userMoneyList.get(i).getId());
			}

			@Override
			public int getBatchSize() {
				return userMoneyList.size();
			}
		});
		if (ArrayUtil.contains(ints, 0)) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("结算更新回滚了");
			throw new ServiceException("更新资产结算更新回滚了");
		}
	}



	/**
	 * 对usdt资产增加
	 *
	 * @param userMoneyList
	 */
	private void bachUpdateMoneyValid1(List<UserMoney> userMoneyList,Integer coinType) {
		int[] ints = jdbcTemplate.batchUpdate(coinType == 1?SQL_VALID_NUM1:SQL_VALID_NUM2, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				ps.setTimestamp(1, new java.sql.Timestamp(userMoneyList.get(i).getUpdateTime().getTime()));
				ps.setString(2, userMoneyList.get(i).getGtId());
				ps.setBigDecimal(3, userMoneyList.get(i).getValidNum1());
				ps.setString(4, userMoneyList.get(i).getSourceCode());
				ps.setInt(5, userMoneyList.get(i).getSourceType());
				ps.setLong(6, userMoneyList.get(i).getSourceId());
				ps.setLong(7, userMoneyList.get(i).getId());
			}

			@Override
			public int getBatchSize() {
				return userMoneyList.size();
			}
		});
		if (ArrayUtil.contains(ints, 0)) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("结算更新回滚了");
			throw new ServiceException("更新资产结算更新回滚了");
		}
	}

	/**
	 * 对usdt资产增加
	 *
	 * @param userMoneyList
	 */
	private void bachUpdateMoneyValid1(List<UserMoney> userMoneyList) {
		int[] ints = jdbcTemplate.batchUpdate(SQL_VALID_NUM1, new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {

				ps.setTimestamp(1, new java.sql.Timestamp(userMoneyList.get(i).getUpdateTime().getTime()));
				ps.setString(2, userMoneyList.get(i).getGtId());
				ps.setBigDecimal(3, userMoneyList.get(i).getValidNum1());
				ps.setString(4, userMoneyList.get(i).getSourceCode());
				ps.setInt(5, userMoneyList.get(i).getSourceType());
				ps.setLong(6, userMoneyList.get(i).getSourceId());
				ps.setLong(7, userMoneyList.get(i).getId());
			}

			@Override
			public int getBatchSize() {
				return userMoneyList.size();
			}
		});
		if (ArrayUtil.contains(ints, 0)) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			log.error("结算更新回滚了");
			throw new ServiceException("更新资产结算更新回滚了");
		}
	}
}
