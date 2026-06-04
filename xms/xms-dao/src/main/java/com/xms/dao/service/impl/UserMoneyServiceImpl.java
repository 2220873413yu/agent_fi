package com.xms.dao.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.xms.common.config.redis.lock.RedisLock;
import com.xms.common.constant.ConstantType;
import com.xms.common.constant.RedisConstant;
import com.xms.common.exception.ServiceException;
import com.xms.common.utils.SecurityUtils;
import com.xms.common.utils.uuid.IDUtils;
import com.xms.dao.entity.bo.GrantRewardTransferBo;
import com.xms.dao.entity.bo.UserMoneyValidNum4Bo;
import com.xms.dao.entity.domain.UserMoney;
import com.xms.dao.entity.vo.UpdateUserWalletVo;
import com.xms.dao.entity.vo.UpdateUserMoneyVo;
import com.xms.dao.entity.vo.UserMoneyLogVo;
import com.xms.dao.entity.vo.UserMoneyVo;
import com.xms.dao.entity.vo.UserWalletLogVo;
import com.xms.dao.mapper.UserMoneyMapper;
import com.xms.dao.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户钱包Service业务层处理
 *
 * @date 2023-07-31
 */
@Service
public class UserMoneyServiceImpl extends ServiceImpl<UserMoneyMapper, UserMoney> implements IUserMoneyService {

	@Autowired
	private UserWalletService userWalletServiceImpl;

	/**
	 * 查询用户钱包列表
	 *
	 * @param userMoney 用户钱包
	 * @return 用户钱包
	 */
	@Override
	public List<UserMoney> selectUserMoneyList(UserMoney userMoney) {
		List<UserMoney> userMonies = baseMapper.selectUserMoneyList(userMoney);
		return userMonies;
	}

	/**
	 * 更新钱包
	 *
	 * @param userMoneyVo
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public int updateUserMoney(UserMoneyVo userMoneyVo) {

		String orderCode =IDUtils.getSnowflakeStr();
		int i = userWalletServiceImpl.handerUserMoney(userMoneyVo.getChangeBalance(), orderCode, userMoneyVo.getId(),
			SecurityUtils.getUserId(), ConstantType.user_money_log_source_type.type_28, userMoneyVo.getCoinType());
		if (i == 0) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new ServiceException("更新资产异常");
		}
		return 1;
	}

	/**
	 * 后台将拨付收益USDT转入用户可用USDT。
	 *
	 * <p>该操作是同一用户钱包字段迁移，不是充值、提现或平台扣拨。方法按用户ID加Redis锁，
	 * 在同一事务内通过钱包框架一次性扣减 `valid_num3` 并增加 `valid_num1`，Canal后续按同一个
	 * transferNo/sourceType=49 生成拨付收益USDT扣减和可用USDT增加两条流水。</p>
	 *
	 * @param req 转移用户和转移金额，金额单位USDT
	 * @return 1表示转移成功
	 */
	@Override
	@RedisLock(value = RedisConstant.LockConstant.XMS_GRANT_REWARD_TRANSFER, param = "#req.userId")
	@Transactional(rollbackFor = Exception.class)
	public int transferGrantRewardToUsdt(GrantRewardTransferBo req) {
		if (req == null || req.getUserId() == null) {
			throw new ServiceException("用户ID不能为空");
		}
		if (req.getTransferAmount() == null || req.getTransferAmount().compareTo(BigDecimal.ZERO) <= 0) {
			throw new ServiceException("转移数量必须大于0");
		}
		BigDecimal transferAmount = req.getTransferAmount();

		// 以后端当前钱包余额为准，前端传入的余额只用于页面即时校验。
		UserMoney userMoney = getById(req.getUserId());
		if (userMoney == null) {
			throw new ServiceException("用户钱包不存在");
		}
		BigDecimal grantRewardBalance = userMoney.getValidNum3() == null ? BigDecimal.ZERO : userMoney.getValidNum3();
		if (grantRewardBalance.compareTo(transferAmount) < 0) {
			throw new ServiceException("拨付收益USDT余额不足");
		}

		// 同一个transferNo/sourceType追踪本次字段迁移，Canal会拆成coinType=3扣减和coinType=1入账两条流水。
		String transferNo = IDUtils.getSnowflakeStr();
		Long adminUserId = SecurityUtils.getUserId();
		UpdateUserWalletVo updateUserWalletVo = UpdateUserWalletVo.builder()
			.userId(req.getUserId())
			.gtId(IDUtils.getSnowflakeStr())
			.sourceCode(transferNo)
			.sourceId(adminUserId)
			.sourceType(ConstantType.user_money_log_source_type.type_49)
			.userWalletLogList(Lists.newArrayList(
				UserWalletLogVo.builder()
					.coinType(ConstantType.user_money_coin_type.type_3)
					.changeBalance(transferAmount.negate())
					.build(),
				UserWalletLogVo.builder()
					.coinType(ConstantType.user_money_coin_type.type_1)
					.changeBalance(transferAmount)
					.build()
			))
			.build();
		int rows = userWalletServiceImpl.updateWallet(updateUserWalletVo);
		if (rows != 1) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			throw new ServiceException("拨付收益USDT余额不足");
		}
		return 1;
	}

	/**
	 * 查询链信值余额大于sourceThreshold的钱包用户
	 * @param sourceThreshold
	 * @return
	 */
	@Override
	public List<UserMoneyValidNum4Bo> queryGeSourceThresholdId(BigDecimal sourceThreshold) {
		return baseMapper.queryGeSourceThresholdId(sourceThreshold);
	}


	/**
	 * canal 数据同步的方式处理，如果不采用，那自行处理流水。
	 * 即将废弃 新版本可采用UserWalletService.handerUserMoney 方法,或者自己写原始SQL
	 *
	 * @param updateUserMoneyVo
	 * @return int
	 * @Title: updateUserMoney
	 * @param:
	 * @Description: 更新用户钱包
	 */
	@Override
	public int updateUserMoney(UpdateUserMoneyVo updateUserMoneyVo) {
		//批量新增流水对象
		UserMoney userMoney = UserMoney.builder().id(updateUserMoneyVo.getUserId()).build();
		List<UserMoneyLogVo> userMoneyLogList = updateUserMoneyVo.getUserMoneyLogList();
		for (UserMoneyLogVo userMoneyLogVo : userMoneyLogList) {
			if (userMoneyLogVo.getChangeBalance().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}
			if (updateUserMoneyVo.getSourceType() == null) {
				updateUserMoneyVo.setSourceType(userMoneyLogVo.getSourceType());
			}
			if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_1) {
				userMoney.setValidNum1(userMoneyLogVo.getChangeBalance());
			} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_2) {
				userMoney.setValidNum2(userMoneyLogVo.getChangeBalance());
			} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_3) {
				userMoney.setValidNum3(userMoneyLogVo.getChangeBalance());
			} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_4) {
				userMoney.setValidNum4(userMoneyLogVo.getChangeBalance());
			} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_5) {
				userMoney.setValidNum5(userMoneyLogVo.getChangeBalance());
			} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_6) {
				userMoney.setValidNum6(userMoneyLogVo.getChangeBalance());
			} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_7) {
				userMoney.setValidNum7(userMoneyLogVo.getChangeBalance());
			} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_8) {
				userMoney.setValidNum8(userMoneyLogVo.getChangeBalance());
			} else if (userMoneyLogVo.getCoinType() == ConstantType.user_money_coin_type.type_9) {
				userMoney.setValidNum9(userMoneyLogVo.getChangeBalance());
			}

		}
		//更新钱包
		userMoney.setGtId(IDUtils.getSnowflake().nextIdStr());
		userMoney.setSourceCode(updateUserMoneyVo.getSourceCode());
		userMoney.setSourceId(updateUserMoneyVo.getSourceId() == null ? updateUserMoneyVo.getUserId() : updateUserMoneyVo.getSourceId());
		userMoney.setSourceType(updateUserMoneyVo.getSourceType());
		int i = this.baseMapper.updateUserMoney(userMoney);
		if (i != 1) {
			return 0;
		}
		return 1;
	}

	@Override
	public BigDecimal querySubReward(Long userId) {
		return baseMapper.querySubReward(userId);
	}

	@Override
	public BigDecimal queryIndirectReward(Long userId) {
		return baseMapper.queryIndirectReward(userId);
	}

	@Override
	public BigDecimal getTodayReward(Long userId) {
		return baseMapper.getTodayReward(userId);
	}

	@Override
	public BigDecimal getTotalReward(Long userId) {
		return baseMapper.getTotalReward(userId);
	}
}
