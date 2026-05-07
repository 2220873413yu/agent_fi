package com.xms.dao.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xms.common.config.redis.XmsRedis;
import com.xms.common.constant.ConstantStatic;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.constant.SysConstant;
import com.xms.dao.domain.*;
import com.xms.dao.entity.bo.UserMoneySumDTO;
import com.xms.dao.entity.domain.UserInfo;
import com.xms.dao.entity.domain.Withdrawal;
import com.xms.dao.entity.domain.UserMoneyLog;
import com.xms.dao.entity.vo.IndexDataPanelVo;
import com.xms.dao.mapper.SysParaMapper;
import com.xms.dao.mapper.UserInfoMapper;
import com.xms.dao.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class IndexDataServiceImpl implements IndexDataService {

	@Autowired
	private UserInfoMapper userInfoMapper;

	@Autowired
	private XmsRedis xmsRedis;

	@Autowired
	private SysParaMapper sysParaMapper;

	@Resource(name = "asyncExecutor")
	private Executor asyncExecutor;

	@Autowired
	private UserInfoService userInfoService;

	@Autowired
	private IMiningPackageOrderService miningPackageOrderService;

	@Autowired
	private IStakeOrderService stakeOrderService;

	@Autowired
	private INodePackageOrderService nodePackageOrderService;

	@Autowired
	private IRewardRecordService rewardRecordService;

	@Autowired
	private IStakeReleaseBucketService stakeReleaseBucketService;

	@Autowired
	private IRechargeRecordService rechargeRecordService;

	@Autowired
	private WithdrawalService withdrawalService;

	@Autowired
	private IUserTransferService userTransferService;

	/**
	 * 计算跌幅百分比
	 *
	 * @param previousPrice 前一天的价格
	 * @param currentPrice  今天的价格
	 * @return 跌幅百分比，如果前一天价格为零则返回 null
	 */
	public static BigDecimal calculateDeclinePercentage(BigDecimal previousPrice, BigDecimal currentPrice) {
		if (previousPrice.compareTo(BigDecimal.ZERO) == 0) {
			// 返回 null 表示无法计算
			return BigDecimal.ZERO;
		}
		BigDecimal difference = previousPrice.subtract(currentPrice);
		BigDecimal declinePercentage = difference.divide(SysConstant.BAIFENBI, ConstantStatic.newScale, ConstantStatic.roundingModeNew).multiply(SysConstant.BAIFENBI).setScale(ConstantStatic.newScale, ConstantStatic.roundingModeNew);
		return declinePercentage;
	}

	@Override
	public IndexDataPanelVo getIndexDataPanelVo() {
		IndexDataPanelVo indexDataPanelVo = new IndexDataPanelVo();
		indexDataPanelVo.setV10(BigDecimal.ZERO);

		//质押订单量
		BigDecimal v6 = new BigDecimal(stakeOrderService.lambdaQuery()
			.count());
		indexDataPanelVo.setV6(v6);

		BigDecimal v11 = BigDecimal.ZERO;
//			yieldedSum == null || yieldedSum.getYieldedAmount() == null
//			? BigDecimal.ZERO
//			: yieldedSum.getYieldedAmount();
		indexDataPanelVo.setV11(v11);


		//计算锁仓总量
		StakeReleaseBucket totalRemainingAmountBucket = stakeReleaseBucketService.getOne(
			new QueryWrapper<StakeReleaseBucket>()
				.select("IFNULL(SUM(remaining_amount),0) AS remaining_amount"),
			false
		);
		BigDecimal v12 = totalRemainingAmountBucket == null || totalRemainingAmountBucket.getRemainingAmount() == null
			? BigDecimal.ZERO
			: totalRemainingAmountBucket.getRemainingAmount();
		indexDataPanelVo.setV12(v12);

		//计算已释放总量
		StakeReleaseBucket releasedAmountBucket = stakeReleaseBucketService.getOne(
			new QueryWrapper<StakeReleaseBucket>()
				.select("IFNULL(SUM(total_amount - remaining_amount),0) AS total_amount"),
			false
		);
		BigDecimal v13 = releasedAmountBucket == null || releasedAmountBucket.getTotalAmount() == null
			? BigDecimal.ZERO
			: releasedAmountBucket.getTotalAmount();
		indexDataPanelVo.setV13(v13);


		//全网服务身份数量
		BigDecimal v7 =BigDecimal.ZERO;
			//userInfoMapper.userTotalComputingPower();
		indexDataPanelVo.setV7(v7);

		//全网质押节点量
		BigDecimal v8 = new BigDecimal(nodePackageOrderService.lambdaQuery()
			.in(NodePackageOrder::getStatus,1).count());
		indexDataPanelVo.setV8(v8);
		//今天质押节点量
		BigDecimal v9 =  new BigDecimal(nodePackageOrderService.lambdaQuery()
			.eq(NodePackageOrder::getStatus,1)
			.apply("create_time >= CURDATE()")
			.count());
		indexDataPanelVo.setV9(v9);

		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			//全网余额信息
			//indexDataPanelVo.setV3(userInfoService.lambdaQuery().eq(UserInfo::getIsValid, 1).apply("create_time >= CURDATE()").count());
			UserMoneySumDTO userMoneySumDTO = userInfoMapper.queryUserMoneySum();
			indexDataPanelVo.setV26(userMoneySumDTO.getTotalValidNum1());
			indexDataPanelVo.setV27(userMoneySumDTO.getTotalValidNum2());
			/*indexDataPanelVo.setV28(userMoneySumDTO.getTotalValidNum3());*/
		}, asyncExecutor);

		CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {/*
			//统计质押订单金额.用.apply函数计算出total_amount
			BigDecimal v36 = stakeOrderService.lambdaQuery()
				.in(StakeOrder::getStatus,1,2)
				.select(StakeOrder::getStakeUsdtAmount)
				.list().stream()
				.map(StakeOrder::getStakeUsdtAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			//统计未出局的订单金额
			BigDecimal v37 = stakeOrderService.lambdaQuery()
				.in(StakeOrder::getStatus,1)
				.select(StakeOrder::getRemainingOutAmount)
				.list().stream()
				.map(StakeOrder::getRemainingOutAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			indexDataPanelVo.setV36(v36);
			indexDataPanelVo.setV37(v37);
			//查询今日的静奖励金额
			BigDecimal v38 = rewardRecordService.lambdaQuery()
				.eq(RewardRecord::getSourceType,6)
				.select(RewardRecord::getAmount)
				.apply("create_time >= CURDATE()")
				.list().stream()
				.map(RewardRecord::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			indexDataPanelVo.setV38(v38);

			//查询今日的动奖励金额
			BigDecimal v39 = rewardRecordService.lambdaQuery()
				.in(RewardRecord::getSourceType,1,2,3,4,5)
				.select(RewardRecord::getAmount)
				.apply("create_time >= CURDATE()")
				.list().stream()
				.map(RewardRecord::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
			indexDataPanelVo.setV39(v39);*/
		}, asyncExecutor);

		CompletableFuture<Void> future4 = CompletableFuture.runAsync(() -> {



			BigDecimal v28 = BigDecimal.valueOf(userInfoService.lambdaQuery()
					.eq(UserInfo::getNodeLevel,1)
				.count());

			BigDecimal v29 = BigDecimal.valueOf(userInfoService.lambdaQuery()
				.eq(UserInfo::getNodeLevel,2)
				.count());
			BigDecimal v30 = BigDecimal.valueOf(userInfoService.lambdaQuery()
				.eq(UserInfo::getNodeLevel,3)
				.count());
			/*BigDecimal v31 = BigDecimal.valueOf(userInfoService.lambdaQuery()
				.apply("GREATEST(IFNULL(game_level, 0), IFNULL(min_game_level, 0), IFNULL(admin_game_level, 0)) = 4")
				.count());*/
			indexDataPanelVo.setV28(v28);
			indexDataPanelVo.setV29(v29);
			indexDataPanelVo.setV30(v30);
			indexDataPanelVo.setV31(BigDecimal.ZERO);

			//提现币种 1:USDT,2:DFC,3:OORT,5:产出DFC
			Withdrawal usdtWdwSum = withdrawalService.getOne(
				new QueryWrapper<Withdrawal>()
					.select("IFNULL(SUM(change_balance),0) AS change_balance")
					.eq("status", 3)
					.eq("coin_type", 1),
				false
			);
			//累计提现USDT
			BigDecimal v32 = usdtWdwSum == null || usdtWdwSum.getChangeBalance() == null
				? BigDecimal.ZERO
				: usdtWdwSum.getChangeBalance();
			Withdrawal dfcWdwSum = withdrawalService.getOne(
				new QueryWrapper<Withdrawal>()
					.select("IFNULL(SUM(change_balance),0) AS change_balance")
					.eq("status", 3)
					.eq("coin_type", 2),
				false
			);
			//累计提现DFC
			BigDecimal v33 = dfcWdwSum == null || dfcWdwSum.getChangeBalance() == null
				? BigDecimal.ZERO
				: dfcWdwSum.getChangeBalance();
			Withdrawal oortWdwSum = withdrawalService.getOne(
				new QueryWrapper<Withdrawal>()
					.select("IFNULL(SUM(change_balance),0) AS change_balance")
					.eq("status", 3)
					.eq("coin_type", 3),
				false
			);
			//累计提现OORT
			BigDecimal v34 = oortWdwSum == null || oortWdwSum.getChangeBalance() == null
				? BigDecimal.ZERO
				: oortWdwSum.getChangeBalance();
			Withdrawal outputDfcWdwSum = withdrawalService.getOne(
				new QueryWrapper<Withdrawal>()
					.select("IFNULL(SUM(change_balance),0) AS change_balance")
					.eq("status", 3)
					.eq("coin_type", 5),
				false
			);
			//累计提现产出DFC
			BigDecimal v35 = outputDfcWdwSum == null || outputDfcWdwSum.getChangeBalance() == null
				? BigDecimal.ZERO
				: outputDfcWdwSum.getChangeBalance();
			indexDataPanelVo.setV32(v32);
			indexDataPanelVo.setV33(v33);
			indexDataPanelVo.setV34(v34);
			indexDataPanelVo.setV35(v35);
		}, asyncExecutor);

		CompletableFuture.allOf(future2, future3,future4).join();
		return indexDataPanelVo;
	}

	public String getValue(String code) {
		return xmsRedis.get(RedisConstant.XMS_PARAM + code, () -> sysParaMapper.getValue(code), 15L, TimeUnit.DAYS);
	}
}
